package com.darkmintis.gitstore.core.presentation.components

import com.darkmintis.gitstore.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.darkmintis.gitstore.core.presentation.utils.GitStoreLinks
import com.darkmintis.gitstore.core.presentation.utils.openWebUrl

@Composable
fun RetryErrorState(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryText: String = stringResource(R.string.retry),
    showReportIssue: Boolean = message == stringResource(R.string.error_unknown)
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        GithubStoreButton(
            text = retryText,
            onClick = onRetry
        )

        if (showReportIssue) {
            Spacer(modifier = Modifier.height(8.dp))
            ReportIssueButton()
        }
    }
}

@Composable
fun ReportIssueButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    TextButton(
        onClick = { context.openWebUrl(GitStoreLinks.NEW_BUG_REPORT) },
        modifier = modifier
    ) {
        Text(text = stringResource(R.string.report_an_issue))
    }
}
