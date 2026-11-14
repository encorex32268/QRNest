@file:OptIn(ExperimentalMaterial3Api::class)

package com.lihan.qrnest.feature.generate.presentation.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lihan.qrnest.core.designsystem.components.QRNestButton
import com.lihan.qrnest.core.designsystem.components.QRNestIconButton
import com.lihan.qrnest.core.designsystem.icon.QRNestIcons
import com.lihan.qrnest.core.designsystem.resources.Res
import com.lihan.qrnest.core.designsystem.resources.back
import com.lihan.qrnest.core.designsystem.resources.email
import com.lihan.qrnest.core.designsystem.resources.encryption_type
import com.lihan.qrnest.core.designsystem.resources.generate_qr_code
import com.lihan.qrnest.core.designsystem.resources.latitude
import com.lihan.qrnest.core.designsystem.resources.link
import com.lihan.qrnest.core.designsystem.resources.longitude
import com.lihan.qrnest.core.designsystem.resources.name
import com.lihan.qrnest.core.designsystem.resources.password
import com.lihan.qrnest.core.designsystem.resources.phone_number
import com.lihan.qrnest.core.designsystem.resources.ssid
import com.lihan.qrnest.core.designsystem.resources.text
import com.lihan.qrnest.core.designsystem.theme.QRNestTheme
import com.lihan.qrnest.core.designsystem.theme.appColors
import com.lihan.qrnest.core.domain.model.QRCodeType
import com.lihan.qrnest.core.presentation.util.ObserveAsEvents
import com.lihan.qrnest.core.presentation.util.asString
import com.lihan.qrnest.feature.generate.presentation.components.CreateQRTextField
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateScreenRoot(
    onNavigateToPreview: (id: Long) -> Unit,
    onBack: () -> Unit,
    viewModel: CreateViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is CreateEvent.NavigateToPreview -> onNavigateToPreview(event.id)
        }
    }

    CreateScreen(
        state = state,
        onAction = { action ->
            when (action) {
                CreateAction.BackClick -> onBack()
                else -> viewModel.onAction(action)
            }
        }
    )
}

@Composable
fun CreateScreen(
    state: CreateState,
    onAction: (CreateAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    val focusRequesterSecond = remember { FocusRequester() }
    val focusRequesterThird = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            title = {
                Text(
                    text = state.qrCodeType.asString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            navigationIcon = {
                QRNestIconButton(
                    imageVector = QRNestIcons.ArrowLeft,
                    contentDescription = stringResource(Res.string.back),
                    containerColor = Color.Transparent,
                    tintColor = MaterialTheme.colorScheme.onSurface,
                    iconSize = 20.dp,
                    onClick = { onAction(CreateAction.BackClick) }
                )
            }
        )

        Surface(
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.appColors.surfaceHigher,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (state.qrCodeType) {
                    QRCodeType.Text,
                    QRCodeType.PhoneNumber,
                    QRCodeType.Link -> {
                        CreateQRTextField(
                            value = state.firstInput,
                            onValueChange = { onAction(CreateAction.OnFirstInputChanged(it)) },
                            placeholder = when (state.qrCodeType) {
                                QRCodeType.Link -> stringResource(Res.string.link)
                                QRCodeType.PhoneNumber -> stringResource(Res.string.phone_number)
                                else -> stringResource(Res.string.text)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = when (state.qrCodeType) {
                                    QRCodeType.Link -> KeyboardType.Uri
                                    QRCodeType.PhoneNumber -> KeyboardType.Phone
                                    else -> KeyboardType.Text
                                },
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus(true)
                                    keyboard?.hide()
                                }
                            )
                        )
                    }

                    QRCodeType.Geolocation -> {
                        CreateQRTextField(
                            value = state.firstInput,
                            onValueChange = { onAction(CreateAction.OnFirstInputChanged(it)) },
                            placeholder = stringResource(Res.string.latitude),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusRequesterSecond.requestFocus() }
                            )
                        )
                        CreateQRTextField(
                            modifier = Modifier.focusRequester(focusRequesterSecond),
                            value = state.secondInput,
                            onValueChange = { onAction(CreateAction.OnSecondInputChanged(it)) },
                            placeholder = stringResource(Res.string.longitude),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus(true)
                                    keyboard?.hide()
                                }
                            )
                        )
                    }

                    QRCodeType.WiFi,
                    QRCodeType.Contact -> {
                        CreateQRTextField(
                            value = state.firstInput,
                            onValueChange = { onAction(CreateAction.OnFirstInputChanged(it)) },
                            placeholder = if (state.qrCodeType == QRCodeType.WiFi) {
                                stringResource(Res.string.ssid)
                            } else {
                                stringResource(Res.string.name)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusRequesterSecond.requestFocus() }
                            )
                        )
                        CreateQRTextField(
                            modifier = Modifier.focusRequester(focusRequesterSecond),
                            value = state.secondInput,
                            onValueChange = { onAction(CreateAction.OnSecondInputChanged(it)) },
                            placeholder = if (state.qrCodeType == QRCodeType.WiFi) {
                                stringResource(Res.string.password)
                            } else {
                                stringResource(Res.string.email)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (state.qrCodeType == QRCodeType.WiFi) {
                                    KeyboardType.Password
                                } else {
                                    KeyboardType.Email
                                },
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusRequesterThird.requestFocus() }
                            )
                        )
                        CreateQRTextField(
                            modifier = Modifier.focusRequester(focusRequesterThird),
                            value = state.thirdInput,
                            onValueChange = { onAction(CreateAction.OnThirdInputChanged(it)) },
                            placeholder = if (state.qrCodeType == QRCodeType.WiFi) {
                                stringResource(Res.string.encryption_type)
                            } else {
                                stringResource(Res.string.phone_number)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (state.qrCodeType == QRCodeType.Contact) {
                                    KeyboardType.Phone
                                } else {
                                    KeyboardType.Text
                                },
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus(true)
                                    keyboard?.hide()
                                }
                            )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                QRNestButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.generate_qr_code),
                    onClick = {
                        focusManager.clearFocus(true)
                        keyboard?.hide()
                        onAction(CreateAction.GenerateButtonClick)
                    },
                    enabled = state.generateButtonEnabled && !state.isGenerating,
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview
@Composable
private fun CreateScreenTextPreview() {
    QRNestTheme {
        CreateScreen(
            state = CreateState(qrCodeType = QRCodeType.Text),
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun CreateScreenWifiPreview() {
    QRNestTheme {
        CreateScreen(
            state = CreateState(
                qrCodeType = QRCodeType.WiFi,
                firstInput = "Home_WiFi",
                secondInput = "secret",
                thirdInput = "WPA2",
                generateButtonEnabled = true
            ),
            onAction = {}
        )
    }
}
