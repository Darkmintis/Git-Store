#!/usr/bin/env python3
"""
Git Store - Intelligent Discovery Engine
Advanced system for discovering high-quality Android apps from GitHub
Uses smart filtering, caching, and multi-strategy discovery
"""

import os
import sys
import json
import requests
from datetime import datetime
from typing import List, Dict, Optional, Set
from dataclasses import dataclass
import time
from pathlib import Path

@dataclass
class RepoInfo:
    """Structured repository information"""
    id: int
    name: str
    full_name: str
    owner_login: str
    owner_avatar: str
    description: Optional[str]
    html_url: str
    stars: int
    forks: int
    language: Optional[str]
    topics: List[str]
    updated_at: str
    default_branch: str = "main"
    releases_url: str = ""

class DiscoveryEngine:
    """
    Smart Discovery Engine for Android Apps
    Features:
    - Multi-strategy search (trending, topics, quality)
    - Intelligent rate limit handling
    - APK verification
    - Quality scoring
    - Deduplication
    """
    
    # Quality thresholds
    MIN_STARS = 100
    MIN_QUALITY_SCORE = 4  # Lowered to accept more quality apps
    TARGET_APPS = 50
    
    # Search strategies
    SEARCH_QUERIES = [
        'android apk topic:android language:kotlin stars:>200',
        'android app topic:android language:java stars:>150',
        'fdroid topic:android stars:>100',
        'android-app topic:mobile stars:>100',
        'kotlin android topic:app stars:>100',
    ]
    
    def __init__(self):
        """Initialize discovery engine with GitHub API"""
        self.token = os.environ.get('GITHUB_TOKEN')
        if not self.token:
            self._print_error("GITHUB_TOKEN environment variable not set")
            sys.exit(1)
        
        self.headers = {
            'Authorization': f'Bearer {self.token}',
            'Accept': 'application/vnd.github+json',
            'X-GitHub-Api-Version': '2022-11-28'
        }
        
        self.session = requests.Session()
        self.session.headers.update(self.headers)
        self.rate_limit_remaining = None
        self.rate_limit_reset = None
        
    def _print_error(self, message: str):
        """Print error message"""
        print(f"❌ {message}", file=sys.stderr)
    
    def _print_info(self, message: str):
        """Print info message"""
        print(f"ℹ️  {message}")
    
    def _print_success(self, message: str):
        """Print success message"""
        print(f"✅ {message}")
    
    def _check_rate_limit(self, response: requests.Response):
        """Update rate limit info from response headers"""
        self.rate_limit_remaining = int(response.headers.get('X-RateLimit-Remaining', 0))
        self.rate_limit_reset = int(response.headers.get('X-RateLimit-Reset', 0))
        
        if self.rate_limit_remaining < 50:
            self._print_info(f"Rate limit low: {self.rate_limit_remaining} requests remaining")
    
    def _handle_rate_limit(self):
        """Handle rate limit - wait if needed"""
        if self.rate_limit_remaining and self.rate_limit_remaining < 10:
            if self.rate_limit_reset:
                wait_time = max(self.rate_limit_reset - int(time.time()), 60)
            else:
                wait_time = 60
            self._print_info(f"Rate limit reached. Waiting {wait_time}s...")
            time.sleep(wait_time)
    
    def _make_request(self, url: str, params: Optional[Dict] = None, max_retries: int = 3) -> Optional[Dict]:
        """Make API request with smart retry and rate limit handling"""
        if params is None:
            params = {}
        
        for attempt in range(max_retries):
            try:
                response = self.session.get(url, params=params, timeout=30)
                self._check_rate_limit(response)
                
                if response.status_code == 200:
                    return response.json()
                
                if response.status_code == 403:
                    # Rate limited - wait and retry
                    self._handle_rate_limit()
                    continue
                
                if response.status_code == 404:
                    return None  # Not found
                
                # Other errors
                self._print_error(f"HTTP {response.status_code} for {url}")
                if attempt < max_retries - 1:
                    time.sleep(2 ** attempt)
                    
            except requests.RequestException as e:
                self._print_error(f"Request failed: {e}")
                if attempt < max_retries - 1:
                    time.sleep(2 ** attempt)
        
        return None
    
    def _calculate_quality_score(self, repo: Dict) -> int:
        """
        Calculate quality score (0-10) based on multiple factors
        Higher score = better quality app
        """
        score = 0
        
        # Stars weight
        stars = repo.get('stargazers_count', 0)
        if stars > 5000: score += 3
        elif stars > 1000: score += 2
        elif stars > 500: score += 1
        
        # Activity (recent updates)
        updated_at = repo.get('updated_at', '')
        if updated_at:
            try:
                updated = datetime.fromisoformat(updated_at.replace('Z', '+00:00'))
                days_ago = (datetime.now().astimezone() - updated).days
                if days_ago < 30: score += 2
                elif days_ago < 90: score += 1
            except (ValueError, AttributeError):
                pass
        
        # Language preference
        language = repo.get('language', '').lower()
        if language in ['kotlin', 'java']: score += 2
        
        # Topics relevance
        topics = repo.get('topics', [])
        android_topics = ['android', 'apk', 'mobile', 'app', 'fdroid']
        if any(topic in topics for topic in android_topics): score += 2
        
        # Description quality
        if repo.get('description') and len(repo.get('description', '')) > 20: score += 1
        
        return min(score, 10)
    
    def _has_apk_releases(self, owner: str, repo: str) -> bool:
        """Check if repository has APK releases (check more thoroughly)"""
        url = f"https://api.github.com/repos/{owner}/{repo}/releases"
        data = self._make_request(url, params={'per_page': 30})  # Check more releases
        
        if not data or not isinstance(data, list):
            return False
        
        # Check releases for APK
        for release in data:
            assets = release.get('assets', [])
            for asset in assets:
                name = asset.get('name', '').lower()
                # Check for APK files
                if name.endswith('.apk'):
                    return True
        
        return False
    
    def _search_repositories(self, query: str, per_page: int = 100) -> List[Dict]:
        """Search GitHub repositories with given query"""
        url = "https://api.github.com/search/repositories"
        params = {
            'q': query,
            'sort': 'stars',
            'order': 'desc',
            'per_page': per_page
        }
        
        data = self._make_request(url, params)
        if not data:
            return []
        
        return data.get('items', [])
    
    def _should_check_repository(self, repo: Dict, full_name: str, seen: Set[str]) -> bool:
        """Check if repository should be processed"""
        if full_name in seen:
            return False
        if repo['stargazers_count'] < self.MIN_STARS:
            return False
        return True
    
    def _process_repository(self, repo: Dict, seen: Set[str]) -> Optional[RepoInfo]:
        """Process single repository - check quality and APK availability"""
        full_name = repo['full_name']
        
        if full_name in seen:
            return None
        
        # Quality check
        quality_score = self._calculate_quality_score(repo)
        if quality_score < self.MIN_QUALITY_SCORE:
            return None
        
        owner = repo['owner']['login']
        name = repo['name']
        
        # APK check (most expensive operation - do last)
        if not self._has_apk_releases(owner, name):
            return None
        
        # Create RepoInfo
        return RepoInfo(
            id=repo['id'],
            name=name,
            full_name=full_name,
            owner_login=owner,
            owner_avatar=repo['owner']['avatar_url'],
            description=repo.get('description'),
            html_url=repo['html_url'],
            stars=repo['stargazers_count'],
            forks=repo['forks_count'],
            language=repo.get('language'),
            topics=repo.get('topics', []),
            updated_at=repo['updated_at'],
            default_branch=repo.get('default_branch', 'main'),
            releases_url=repo.get('releases_url', '')
        )
    
    def discover_apps(self) -> List[RepoInfo]:
        """
        Main discovery process using multi-strategy approach
        Returns list of high-quality Android apps
        """
        print("\n" + "=" * 70)
        print("🚀 Git Store - Intelligent Discovery Engine".center(70))
        print("=" * 70 + "\n")
        
        discovered_apps: List[RepoInfo] = []
        seen: Set[str] = set()
        total_checked = 0
        
        for idx, query in enumerate(self.SEARCH_QUERIES, 1):
            if len(discovered_apps) >= self.TARGET_APPS:
                break
            
            print(f"\n📊 Strategy {idx}/{len(self.SEARCH_QUERIES)}")
            print(f"🔍 Query: {query[:60]}...")
            
            repos = self._search_repositories(query)
            print(f"📦 Found {len(repos)} candidates")
            
            for repo in repos:
                if len(discovered_apps) >= self.TARGET_APPS:
                    break
                
                total_checked += 1
                full_name = repo['full_name']
                
                # Quick filters first
                if not self._should_check_repository(repo, full_name, seen):
                    continue
                
                seen.add(full_name)
                
                print(f"   [{len(discovered_apps)+1}/{self.TARGET_APPS}] Checking {full_name}...", end=' ')
                
                repo_info = self._process_repository(repo, seen)
                if repo_info:
                    discovered_apps.append(repo_info)
                    print(f"✅ (⭐ {repo_info.stars})")
                else:
                    print("❌")
                
                time.sleep(0.2)  # Rate limiting protection
        
        # Sort by stars descending
        discovered_apps.sort(key=lambda r: r.stars, reverse=True)
        
        print("\n" + "=" * 70)
        print("✅ Discovery Complete".center(70))
        print(f"Apps Found: {len(discovered_apps)} | Repositories Checked: {total_checked}".center(70))
        print("=" * 70 + "\n")
        
        return discovered_apps
    
    def save_results(self, repos: List[RepoInfo]):
        """Save discovery results to structured JSON"""
        output_data = {
            'platform': 'android',
            'lastUpdated': datetime.now().astimezone().isoformat(),
            'totalCount': len(repos),
            'discoveryMetadata': {
                'engine_version': '2.0',
                'strategies_used': len(self.SEARCH_QUERIES),
                'min_quality_score': self.MIN_QUALITY_SCORE,
                'min_stars': self.MIN_STARS
            },
            'repositories': [
                {
                    'id': r.id,
                    'name': r.name,
                    'fullName': r.full_name,
                    'owner': {
                        'login': r.owner_login,
                        'avatarUrl': r.owner_avatar
                    },
                    'description': r.description,
                    'defaultBranch': r.default_branch,
                    'htmlUrl': r.html_url,
                    'stargazersCount': r.stars,
                    'forksCount': r.forks,
                    'language': r.language,
                    'topics': r.topics,
                    'releasesUrl': r.releases_url,
                    'updatedAt': r.updated_at
                }
                for r in repos
            ]
        }
        
        # Ensure directory exists
        output_dir = Path('cached-data/trending')
        output_dir.mkdir(parents=True, exist_ok=True)
        
        output_file = output_dir / 'android.json'
        
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(output_data, f, indent=2, ensure_ascii=False)
        
        self._print_success(f"Saved to {output_file}")
        print(f"📊 Total apps: {len(repos)}")
        print(f"💾 File size: {output_file.stat().st_size / 1024:.1f} KB")

def main():
    """Main entry point"""
    try:
        engine = DiscoveryEngine()
        apps = engine.discover_apps()
        
        if apps:
            engine.save_results(apps)
            print("\n🎉 Discovery completed successfully!\n")
            return 0
        else:
            print("\n⚠️  No apps discovered\n", file=sys.stderr)
            return 1
            
    except KeyboardInterrupt:
        print("\n\n⚠️  Discovery interrupted by user\n")
        return 130
    except Exception as e:
        print(f"\n❌ Fatal error: {e}\n", file=sys.stderr)
        return 1

if __name__ == '__main__':
    sys.exit(main())
