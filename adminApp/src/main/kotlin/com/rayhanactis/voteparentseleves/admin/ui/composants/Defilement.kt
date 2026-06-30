package com.rayhanactis.voteparentseleves.admin.ui.composants

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Zone défilante (contenu statique) avec barre de défilement visible — Compose
 * Desktop n'en affiche aucune par défaut. Remplace un
 * `Box(Modifier.verticalScroll(...))`.
 */
@Composable
fun ColonneDefilante(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopCenter,
    content: @Composable BoxScope.() -> Unit
) {
    val defilement = rememberScrollState()
    Box(modifier) {
        Box(
            modifier = Modifier.fillMaxSize().verticalScroll(defilement).padding(end = 12.dp),
            contentAlignment = contentAlignment,
            content = content
        )
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(defilement),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
        )
    }
}

/**
 * `LazyColumn` avec barre de défilement visible. À utiliser comme un `LazyColumn` :
 * le `modifier` dimensionne la zone, la liste la remplit et la barre se cale à droite.
 */
@Composable
fun LazyColonneDefilante(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: LazyListScope.() -> Unit
) {
    Box(modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(end = 12.dp),
            state = state,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = content
        )
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(state),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
        )
    }
}
