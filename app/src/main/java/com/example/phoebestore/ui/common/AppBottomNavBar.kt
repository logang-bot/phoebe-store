package com.example.phoebestore.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.phoebestore.R

@Composable
fun AppBottomNavBar(
    modifier: Modifier = Modifier,
    isHomeSelected: Boolean,
    onHomeClick: () -> Unit,
    onStoresClick: () -> Unit
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = isHomeSelected,
            onClick = onHomeClick,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.nav_home)) }
        )
        NavigationBarItem(
            selected = !isHomeSelected,
            onClick = onStoresClick,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_storefront),
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.nav_stores)) }
        )
    }
}
