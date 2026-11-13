package com.lihan.qrnest.feature.preview.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lihan.qrnest.core.designsystem.components.CircleIcon
import com.lihan.qrnest.core.designsystem.components.QRNestButton
import com.lihan.qrnest.core.designsystem.components.TextLinkButton
import com.lihan.qrnest.core.designsystem.icon.QRNestIcons
import com.lihan.qrnest.core.designsystem.resources.Res
import com.lihan.qrnest.core.designsystem.resources.copy
import com.lihan.qrnest.core.designsystem.resources.save
import com.lihan.qrnest.core.designsystem.resources.share
import com.lihan.qrnest.core.designsystem.resources.show_more
import com.lihan.qrnest.core.designsystem.theme.QRNestTheme
import com.lihan.qrnest.core.designsystem.theme.appColors
import com.lihan.qrnest.core.domain.model.QRCodeType
import com.lihan.qrnest.core.presentation.util.asString
import io.github.alexzhirkevich.qrose.options.QrBackground
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrColors
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import org.jetbrains.compose.resources.stringResource

@Composable
fun QRCodePreviewCard(
    titleInput: String,
    onTitleChanged: (String) -> Unit,
    type: Int,
    savedTitle: String?,
    content: String,
    onPrimaryContentClick: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier,
    qrCodeBackground: Color = MaterialTheme.colorScheme.surface,
    qrCodeColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val qrCodeType = remember(type) { QRCodeType.fromTypeCode(type) }

    var isShowMoreTextShow by remember(content) { mutableStateOf(false) }
    var isShowMoreText by remember(content) { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        if (!isFocused) {
            keyboard?.hide()
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier.padding(top = 80.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp, bottom = 16.dp)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))
                BasicTextField(
                    interactionSource = interactionSource,
                    value = titleInput,
                    onValueChange = onTitleChanged,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.Center) {
                            if (titleInput.isEmpty()) {
                                Text(
                                    modifier = Modifier.alpha(if (isFocused) 0.2f else 1f),
                                    text = savedTitle?.takeIf { it.isNotBlank() }
                                        ?: qrCodeType.asString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            innerTextField()
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus(true) }
                    )
                )
                Spacer(Modifier.height(10.dp))
                when (qrCodeType) {
                    QRCodeType.Link -> {
                        TextLinkButton(
                            text = content,
                            onClick = onPrimaryContentClick
                        )
                    }

                    QRCodeType.PhoneNumber -> {
                        TextLinkButton(
                            text = content,
                            onClick = onPrimaryContentClick,
                            color = MaterialTheme.colorScheme.appColors.phone,
                            background = MaterialTheme.colorScheme.appColors.phoneBG
                        )
                    }

                    QRCodeType.Geolocation -> {
                        TextLinkButton(
                            text = content,
                            onClick = onPrimaryContentClick,
                            color = MaterialTheme.colorScheme.appColors.geo,
                            background = MaterialTheme.colorScheme.appColors.geoBG
                        )
                    }

                    QRCodeType.WiFi -> {
                        TextLinkButton(
                            text = content,
                            onClick = onPrimaryContentClick,
                            color = MaterialTheme.colorScheme.appColors.wifi,
                            background = MaterialTheme.colorScheme.appColors.wifiBG
                        )
                    }

                    QRCodeType.Contact -> {
                        TextLinkButton(
                            text = content,
                            onClick = onPrimaryContentClick,
                            color = MaterialTheme.colorScheme.appColors.contact,
                            background = MaterialTheme.colorScheme.appColors.contactBG
                        )
                    }

                    QRCodeType.Text -> {
                        SelectionContainer(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = content,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                overflow = if (isShowMoreText) {
                                    TextOverflow.Visible
                                } else {
                                    TextOverflow.Ellipsis
                                },
                                maxLines = if (isShowMoreText) Int.MAX_VALUE else 6,
                                onTextLayout = { result ->
                                    if (!isShowMoreText && result.hasVisualOverflow) {
                                        isShowMoreTextShow = true
                                    }
                                }
                            )
                        }
                        if (isShowMoreTextShow && !isShowMoreText) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .clickable { isShowMoreText = true },
                                text = stringResource(Res.string.show_more),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.appColors.onSurfaceAlt
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircleIcon(
                        iconTintColor = MaterialTheme.colorScheme.onSurface,
                        backgroundColor = MaterialTheme.colorScheme.appColors.surfaceHigher,
                        imageVector = QRNestIcons.Share,
                        contentDescription = stringResource(Res.string.share),
                        onClick = onShare,
                        size = 44.dp
                    )
                    CircleIcon(
                        iconTintColor = MaterialTheme.colorScheme.onSurface,
                        backgroundColor = MaterialTheme.colorScheme.appColors.surfaceHigher,
                        imageVector = QRNestIcons.Copy,
                        contentDescription = stringResource(Res.string.copy),
                        onClick = onCopy,
                        size = 44.dp
                    )
                    QRNestButton(
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving,
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                imageVector = QRNestIcons.Download,
                                tint = MaterialTheme.colorScheme.onSurface,
                                contentDescription = null
                            )
                        },
                        text = stringResource(Res.string.save),
                        containerColor = MaterialTheme.colorScheme.appColors.surfaceHigher,
                        onClick = onSave
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.appColors.surfaceHigher,
            modifier = Modifier.size(160.dp),
            shadowElevation = 4.dp
        ) {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                painter = rememberQrCodePainter(
                    data = content,
                    background = QrBackground(fill = SolidColor(qrCodeBackground)),
                    colors = QrColors(
                        dark = QrBrush.solid(qrCodeColor),
                        light = QrBrush.solid(qrCodeBackground),
                        frame = QrBrush.solid(qrCodeColor),
                        ball = QrBrush.solid(qrCodeColor)
                    )
                ),
                contentDescription = null
            )
        }
    }
}

@Preview
@Composable
private fun QRCodePreviewCardPreview() {
    QRNestTheme {
        QRCodePreviewCard(
            modifier = Modifier.fillMaxWidth(),
            titleInput = "Office WiFi",
            onTitleChanged = {},
            type = QRCodeType.WiFi.typeCode,
            savedTitle = "Office WiFi",
            content = "SSID: Office\nPassword: secret\nEncryption type: WPA2",
            onPrimaryContentClick = {},
            onShare = {},
            onCopy = {},
            onSave = {},
            isSaving = false
        )
    }
}
