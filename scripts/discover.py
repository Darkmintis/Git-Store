#!/usr/bin/env python3
"""
Git Store - Intelligent Discovery Engine
Advanced system for discovering high-quality Android apps from GitHub
Uses smart filtering, trending analysis, and APK verification
Based on proven fetching_trending.py algorithm
"""

import os
import sys
import json
import requests
from datetime import datetime, timedelta
from typing import List, Dict, Optional, Tuple
import time
from pathlib import Path

# Validate GITHUB_TOKEN early
GITHUB_TOKEN = os.environ.get('GITHUB_TOKEN')
if not GITHUB_TOKEN:
    print("ERROR: GITHUB_TOKEN environment variable is not set or is empty.", file=sys.stderr)
    print("Please set GITHUB_TOKEN before running this script.", file=sys.stderr)
    sys.exit(1)

HEADERS = {
    'Authorization': f'token {GITHUB_TOKEN}',
    'Accept': 'application/vnd.github.v3+json'
}

# Android platform configuration
PLATFORM_CONFIG = {
    'topics': ['android'],
    'installer_extensions': ['.apk'],
    'score_keywords': ['android', 'mobile', 'kotlin', 'java', 'apk']
}

MAX_RETRIES = 5
INITIAL_BACKOFF = 2  # seconds

def exponential_backoff_sleep(attempt: int, retry_after: Optional[int] = None) -> None:
    """Sleep with exponential backoff, respecting Retry-After if provided"""
    if retry_after:
        sleep_time = retry_after
        print(f"Rate limited - sleeping for {sleep_time}s (from Retry-After header)")
    else:
        sleep_time = min(INITIAL_BACKOFF * (2 ** attempt), 60)  # Cap at 60 seconds
        print(f"Backoff attempt {attempt + 1} - sleeping for {sleep_time}s")
    time.sleep(sleep_time)

def make_request_with_retry(url: str, params: Optional[Dict] = None, timeout: int = 30) -> Tuple[Optional[requests.Response], Optional[str]]:
    """
    Make HTTP request with retry logic for rate limits and server errors
    Returns: (response, error_message) - response is None if all retries failed
    """
    for attempt in range(MAX_RETRIES):
        try:
            response = requests.get(url, headers=HEADERS, params=params, timeout=timeout)

            # Success
            if response.status_code == 200:
                return response, None

            # Rate limiting (403 or 429)
            if response.status_code in [403, 429]:
                retry_after = response.headers.get('Retry-After')
                retry_after_int = int(retry_after) if retry_after and retry_after.isdigit() else None

                # Check if this is actually a rate limit (GitHub returns specific message)
                try:
                    error_data = response.json()
                    is_rate_limit = 'rate limit' in error_data.get('message', '').lower()
                except:
                    is_rate_limit = response.status_code == 429

                if is_rate_limit:
                    print(f"Rate limit hit (status {response.status_code}) for {url}")
                    if attempt < MAX_RETRIES - 1:
                        exponential_backoff_sleep(attempt, retry_after_int)
                        continue
                    else:
                        error_msg = f"Rate limit exceeded after {MAX_RETRIES} retries"
                        print(f"ERROR: {error_msg}", file=sys.stderr)
                        return None, error_msg
                else:
                    # 403 but not rate limit (e.g., permission denied)
                    error_msg = f"Access forbidden (403) for {url}: {response.text[:200]}"
                    print(f"ERROR: {error_msg}", file=sys.stderr)
                    return None, error_msg

            # Server errors (5xx)
            if 500 <= response.status_code < 600:
                print(f"Server error {response.status_code} for {url}: {response.text[:200]}")
                if attempt < MAX_RETRIES - 1:
                    exponential_backoff_sleep(attempt)
                    continue
                else:
                    error_msg = f"Server error {response.status_code} after {MAX_RETRIES} retries"
                    print(f"ERROR: {error_msg}", file=sys.stderr)
                    return None, error_msg

            # Other non-200 responses (4xx except 403/429)
            error_msg = f"Request failed with status {response.status_code} for {url}: {response.text[:200]}"
            print(f"ERROR: {error_msg}", file=sys.stderr)
            return None, error_msg

        except requests.Timeout:
            print(f"Timeout for {url} (attempt {attempt + 1}/{MAX_RETRIES})")
            if attempt < MAX_RETRIES - 1:
                exponential_backoff_sleep(attempt)
                continue
            else:
                error_msg = f"Timeout after {MAX_RETRIES} attempts"
                print(f"ERROR: {error_msg}", file=sys.stderr)
                return None, error_msg

        except Exception as e:
            error_msg = f"Exception during request to {url}: {str(e)}"
            print(f"ERROR: {error_msg}", file=sys.stderr)
            if attempt < MAX_RETRIES - 1:
                exponential_backoff_sleep(attempt)
                continue
            else:
                return None, error_msg

    return None, "Max retries exceeded"

def calculate_quality_score(repo: Dict) -> int:
    """Calculate relevance score for a repository"""
    score = 5
    topics = [t.lower() for t in repo.get('topics', [])]
    language = (repo.get('language') or '').lower()
    desc = (repo.get('description') or '').lower()

    keywords = PLATFORM_CONFIG['score_keywords']

    for keyword in keywords:
        if keyword in topics:
            score += 10
        if keyword in desc:
            score += 3

    if language in ['kotlin', 'c++', 'rust', 'c#', 'swift', 'dart', 'java']:
        score += 5

    if 'cross-platform' in topics or 'multiplatform' in topics:
        score += 8

    return score

def check_repo_has_apk_releases(owner: str, repo_name: str) -> bool:
    """Check if repository has APK files in releases with retry logic"""
    url = f'https://api.github.com/repos/{owner}/{repo_name}/releases'

    response, error = make_request_with_retry(url, params={'per_page': 10}, timeout=10)

    if response is None:
        print(f"Failed to check APK releases for {owner}/{repo_name}: {error}")
        return False

    try:
        releases = response.json()

        # Find first stable release (NOT draft, NOT prerelease)
        stable_release = None
        for release in releases:
            if not release.get('draft') and not release.get('prerelease'):
                stable_release = release
                break

        if not stable_release or not stable_release.get('assets'):
            return False

        # Check for APK files
        extensions = PLATFORM_CONFIG['installer_extensions']
        for asset in stable_release['assets']:
            asset_name = asset['name'].lower()
            if any(asset_name.endswith(ext) for ext in extensions):
                return True

        return False

    except Exception as e:
        print(f"Error parsing releases for {owner}/{repo_name}: {e}")
        return False

def build_query(base_query: str, topics: List[str]) -> str:
    """Build GitHub search query with support for multiple topics"""
    if not topics:
        return base_query

    # Create OR condition for multiple topics
    if len(topics) == 1:
        topic_query = f"topic:{topics[0]}"
    else:
        topic_parts = [f"topic:{topic}" for topic in topics]
        topic_query = " OR ".join(topic_parts)
        topic_query = f"({topic_query})"

    return f"{base_query} {topic_query}"

def discover_android_apps(desired_count: int = 50) -> List[Dict]:
    """Discover trending Android repositories with APK releases"""
    print(f"\n{'='*60}")
    print(f"🚀 DISCOVERING ANDROID APPS WITH APK RELEASES")
    print(f"{'='*60}")

    url = 'https://api.github.com/search/repositories'
    topics = PLATFORM_CONFIG['topics']

    results: List[Dict] = []
    seen: set = set()
    attempt = 0
    max_attempts = 4
    min_count = 10  # Ensure at least this many if possible

    while len(results) < desired_count and attempt < max_attempts:
        attempt += 1
        days = 7 * (2 ** (attempt - 1))  # 7, 14, 28, 56 days
        stars_min = max(500 // (2 ** (attempt - 1)), 50)  # 500, 250, 125, 62 -> min 50
        current_topics = topics if attempt < 3 else []  # Drop topics on later attempts to broaden search

        past_date = (datetime.utcnow() - timedelta(days=days)).strftime('%Y-%m-%d')
        base_query = f'stars:>{stars_min} archived:false pushed:>={past_date}'
        query = build_query(base_query, current_topics)

        print(f"\n📊 Attempt {attempt}/{max_attempts}: ")
        print(f"   ⏰ Days: {days} | ⭐ Stars>{stars_min} | 🏷️  Topics: {current_topics or 'none'}")
        print(f"   🔍 Query: {query}")

        page = 1
        max_pages = 10  # Increased to allow more candidates

        while len(results) < desired_count and page <= max_pages:
            print(f"\n   📄 Fetching API page {page}...")

            params = {
                'q': query,
                'sort': 'stars',
                'order': 'desc',
                'per_page': 100,
                'page': page
            }

            response, error = make_request_with_retry(url, params=params, timeout=30)

            if response is None:
                print(f"   ❌ Failed to fetch page {page}: {error}")
                break

            try:
                data = response.json()
                items = data.get('items', [])

                print(f"   📦 Got {len(items)} repositories from API")

                if not items:
                    break

                # Score and filter candidates
                candidates = []
                for repo in items:
                    score = calculate_quality_score(repo)
                    if score >= 5:
                        candidates.append((repo, score))

                # Sort by score and take top 50
                candidates.sort(key=lambda x: x[1], reverse=True)
                candidates = [repo for repo, _ in candidates[:50]]

                print(f"   ✨ Checking {len(candidates)} top candidates for APK releases...")

                # Check each candidate for APK releases
                for repo in candidates:
                    if len(results) >= desired_count:
                        break

                    full_name = repo['full_name']
                    if full_name in seen:
                        continue

                    owner = repo['owner']['login']
                    name = repo['name']

                    print(f"      [{len(results)+1}/{desired_count}] {owner}/{name}...", end=' ')

                    if check_repo_has_apk_releases(owner, name):
                        # Transform to summary format
                        summary = {
                            'id': repo['id'],
                            'name': repo['name'],
                            'fullName': full_name,
                            'owner': {
                                'login': owner,
                                'avatarUrl': repo['owner']['avatar_url']
                            },
                            'description': repo.get('description'),
                            'defaultBranch': repo.get('default_branch', 'main'),
                            'htmlUrl': repo['html_url'],
                            'stargazersCount': repo['stargazers_count'],
                            'forksCount': repo['forks_count'],
                            'language': repo.get('language'),
                            'topics': repo.get('topics', []),
                            'releasesUrl': repo['releases_url'],
                            'updatedAt': repo['updated_at']
                        }
                        results.append(summary)
                        seen.add(full_name)
                        print(f"✅ FOUND (⭐ {repo['stargazers_count']})")
                    else:
                        print(f"❌ No stable APK")

                    seen.add(full_name)  # Add to seen even if no APK to avoid rechecking
                    time.sleep(0.5)  # Rate limit protection

                page += 1

            except Exception as e:
                print(f"   ❌ Error processing page {page}: {e}", file=sys.stderr)
                break

    # Sort final results by stargazers count descending and truncate to desired count
    results.sort(key=lambda x: x['stargazersCount'], reverse=True)
    results = results[:desired_count]

    print(f"\n{'='*60}")
    print(f"✅ DISCOVERY COMPLETE: {len(results)} apps found")
    print(f"{'='*60}\n")

    return results

def main():
    """Main function to discover and save Android apps"""
    timestamp = datetime.utcnow().isoformat() + 'Z'

    print(f"\n🚀 Starting Android App Discovery...")

    repos = discover_android_apps(desired_count=50)

    if not repos:
        print("⚠️  Warning: No apps discovered!", file=sys.stderr)
        sys.exit(1)

    output = {
        'lastUpdated': timestamp,
        'totalCount': len(repos),
        'repositories': repos
    }

    # Save to file
    output_dir = 'cached-data/trending'
    os.makedirs(output_dir, exist_ok=True)

    output_file = f'{output_dir}/apps.json'
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(output, f, indent=2, ensure_ascii=False)

    print(f"✅ Saved {len(repos)} apps to {output_file}")
    print(f"📊 File size: {os.path.getsize(output_file) / 1024:.1f} KB")
    print("\n🎉 Discovery completed successfully!\n")

if __name__ == '__main__':
    main()
