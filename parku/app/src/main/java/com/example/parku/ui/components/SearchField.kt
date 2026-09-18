package com.example.parku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

/** Etiqueta "Search by name" que encabeza las tres pantallas de busqueda. */
@Composable
fun SearchByNameLabel(modifier: Modifier = Modifier) {
    Text(
        text = "Search by name",
        modifier = modifier,
        fontFamily = Inter,
        fontSize = 14.sp,
        lineHeight = 18.9.sp,
        fontWeight = FontWeight.W700,
        color = AppColors.darkText,
    )
}

/** Campo editable. No pide el foco solo: el teclado se abre al tocarlo. */
@Composable
fun SearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.white)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = Inter,
                fontSize = 16.sp,
                color = AppColors.darkText,
            ),
            cursorBrush = SolidColor(AppColors.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSubmit(value) }),
        )
    }
}

/** Misma caja, pero solo mostrando lo que se busco. */
@Composable
fun SearchQueryBox(
    query: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.white)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = query,
            fontFamily = Inter,
            fontSize = 16.sp,
            color = AppColors.darkText,
        )
    }
}

/** Titulo de seccion en versalitas grises, como "SUGGESTED PARKING LOTS". */
@Composable
fun SectionCaption(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        fontFamily = Inter,
        fontSize = 12.sp,
        lineHeight = 16.2.sp,
        letterSpacing = 0.6.sp,
        color = AppColors.greyText,
    )
}

