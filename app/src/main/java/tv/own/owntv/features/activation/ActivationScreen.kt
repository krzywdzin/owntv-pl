package tv.own.owntv.features.activation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import tv.own.owntv.R
import tv.own.owntv.ui.theme.MidnightGlassColors
import tv.own.owntv.ui.theme.MidnightGlassRadii
import tv.own.owntv.ui.theme.MidnightGlassTv

@Composable
fun ActivationScreen(
    onActivated: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivationViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var code by remember { mutableStateOf("") }
    val codeFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) { codeFocus.requestFocus() }
    LaunchedEffect(state) {
        val activated = state as? ActivationUiState.Activated ?: return@LaunchedEffect
        onActivated(activated.profileId)
    }

    val working = state is ActivationUiState.Working
    val statusText = when (val current = state) {
        is ActivationUiState.Working -> stringResource(
            if (current.stage == ActivationStage.VERIFYING) {
                R.string.activation_checking
            } else {
                R.string.activation_configuring
            },
        )
        is ActivationUiState.Problem -> stringResource(current.problem.messageRes)
        else -> null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightGlassColors.Bg000)
            .padding(horizontal = MidnightGlassTv.SafeX, vertical = MidnightGlassTv.SafeY),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            androidx.tv.material3.Text(
                text = stringResource(R.string.activation_title),
                color = MidnightGlassColors.Ink,
                fontSize = 48.sp,
                lineHeight = 56.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(16.dp))
            androidx.tv.material3.Text(
                text = stringResource(R.string.activation_body),
                color = MidnightGlassColors.Ink2,
                fontSize = MidnightGlassTv.MinimumText,
                lineHeight = 30.sp,
            )
            Spacer(Modifier.height(36.dp))
            androidx.tv.material3.Text(
                text = stringResource(R.string.activation_code_label),
                color = MidnightGlassColors.Ink2,
                fontSize = MidnightGlassTv.MinimumText,
                lineHeight = 30.sp,
            )
            Spacer(Modifier.height(12.dp))
            ActivationCodeField(
                code = code,
                enabled = !working,
                focusRequester = codeFocus,
                onCodeChange = {
                    code = ActivationViewModel.normalize(it)
                    viewModel.clearProblem()
                },
            )
            Spacer(Modifier.height(28.dp))
            ActivationButton(
                enabled = !working && code.length == ActivationViewModel.CODE_LENGTH,
                onClick = { viewModel.activate(code) },
            )
            if (statusText != null) {
                Spacer(Modifier.height(24.dp))
                androidx.tv.material3.Text(
                    text = statusText,
                    color = if (state is ActivationUiState.Problem) {
                        MidnightGlassColors.Live
                    } else {
                        MidnightGlassColors.Violet300
                    },
                    fontSize = MidnightGlassTv.MinimumText,
                    lineHeight = 30.sp,
                )
            }
        }
    }
}

@Composable
private fun ActivationCodeField(
    code: String,
    enabled: Boolean,
    focusRequester: FocusRequester,
    onCodeChange: (String) -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    BasicTextField(
        value = code,
        onValueChange = onCodeChange,
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(color = MidnightGlassColors.Ink, fontSize = 28.sp),
        cursorBrush = SolidColor(MidnightGlassColors.Blue300),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            keyboardType = KeyboardType.Ascii,
        ),
        modifier = Modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.hasFocus },
        decorationBox = { innerTextField ->
            Box(
                Modifier
                    .border(
                        width = if (focused) 3.dp else 1.dp,
                        color = if (focused) MidnightGlassColors.Blue300 else MidnightGlassColors.Line,
                        shape = RoundedCornerShape(MidnightGlassRadii.Medium),
                    )
                    .padding(4.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(ActivationViewModel.CODE_LENGTH) { index ->
                        val cursorIndex = code.length.coerceAtMost(ActivationViewModel.CODE_LENGTH - 1)
                        val isCursorCell = focused && index == cursorIndex
                        Box(
                            modifier = Modifier
                                .size(width = 58.dp, height = 64.dp)
                                .background(
                                    MidnightGlassColors.Bg200,
                                    RoundedCornerShape(MidnightGlassRadii.Small),
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isCursorCell) {
                                        MidnightGlassColors.Blue200
                                    } else {
                                        MidnightGlassColors.Ink3
                                    },
                                    shape = RoundedCornerShape(MidnightGlassRadii.Small),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.tv.material3.Text(
                                text = code.getOrNull(index)?.toString().orEmpty(),
                                color = MidnightGlassColors.Ink,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                Box(Modifier.size(1.dp).alpha(0f)) { innerTextField() }
            }
        },
    )
}

@Composable
private fun ActivationButton(enabled: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(MidnightGlassRadii.Medium)
    Box(
        modifier = Modifier
            .width(260.dp)
            .height(64.dp)
            .onFocusChanged { focused = it.hasFocus }
            .focusable(enabled = enabled)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .background(
                color = when {
                    !enabled -> MidnightGlassColors.Bg200
                    focused -> MidnightGlassColors.Blue700
                    else -> MidnightGlassColors.Bg300
                },
                shape = shape,
            )
            .border(
                width = if (focused) 3.dp else 1.dp,
                color = if (focused) MidnightGlassColors.Blue300 else MidnightGlassColors.Line,
                shape = shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        androidx.tv.material3.Text(
            text = stringResource(R.string.activation_action),
            color = if (enabled) MidnightGlassColors.Ink else MidnightGlassColors.InkOff,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private val ActivationProblem.messageRes: Int
    get() = when (this) {
        ActivationProblem.FORMAT -> R.string.activation_error_format
        ActivationProblem.INVALID_CODE -> R.string.activation_error_bad_code
        ActivationProblem.EXPIRED -> R.string.activation_error_expired
        ActivationProblem.DEVICE_BOUND -> R.string.activation_error_device
        ActivationProblem.RATE_LIMITED -> R.string.activation_error_rate_limit
        ActivationProblem.NETWORK -> R.string.activation_error_network
        ActivationProblem.SERVER -> R.string.activation_error_server
        ActivationProblem.CONFIGURATION -> R.string.activation_error_config
        ActivationProblem.SOURCE_SETUP -> R.string.activation_error_source
    }
