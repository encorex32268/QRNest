package com.lihan.qrnest.feature.preview.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.lihan.qrnest.core.presentation.util.toBackgroundColor
import com.lihan.qrnest.core.presentation.util.toIconTint
import com.lihan.qrnest.core.presentation.util.toImageVector
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
    qrCodeBackground: Color = Color.White,
    qrCodeColor: Color = Color(0xFF14171A)
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

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.appColors.surfaceHigher,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(qrCodeType.toBackgroundColor())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    imageVector = qrCodeType.toImageVector(),
                    contentDescription = null,
                    tint = qrCodeType.toIconTint()
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = qrCodeType.asString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = qrCodeType.toIconTint()
                )
            }
            Spacer(Modifier.height(20.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = qrCodeBackground,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.size(188.dp)
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
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
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
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
                            TextButton(
                                modifier = Modifier.align(Alignment.Start),
                                onClick = { isShowMoreText = true }
                            ) {
                                Text(
                                    text = stringResource(Res.string.show_more),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.appColors.onSurfaceAlt
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PreviewActionIconButton(
                        imageVector = QRNestIcons.Share,
                        contentDescription = stringResource(Res.string.share),
                        onClick = onShare
                    )
                    PreviewActionIconButton(
                        imageVector = QRNestIcons.Copy,
                        contentDescription = stringResource(Res.string.copy),
                        onClick = onCopy
                    )
                    QRNestButton(
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving,
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                imageVector = QRNestIcons.Download,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                contentDescription = null
                            )
                        },
                        text = stringResource(Res.string.save),
                        textColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.primary,
                        onClick = onSave
                    )
                }
            }
        }
}

@Composable
private fun PreviewActionIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedIconButton(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = imageVector,
            contentDescription = contentDescription
        )
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
