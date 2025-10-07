package org.muilab.noti.summary.view.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.muilab.noti.summary.R
import org.muilab.noti.summary.model.APIKeyEntity
import org.muilab.noti.summary.view.component.NoPaddingAlertDialog
import org.muilab.noti.summary.viewModel.APIKeyViewModel

@Composable
fun APIKeyScreen(apiKeyViewModel: APIKeyViewModel) {
    MaterialTheme {
        APIKeyList(apiKeyViewModel)
        AddKeyButton(apiKeyViewModel)
    }
}

@Composable
fun APICreationLink() {

    val uriHandler = LocalUriHandler.current

    val annotatedLinkString: AnnotatedString = buildAnnotatedString {
        val annotStr = stringResource(R.string.create_api_key)
        val startIndex = 0
        val endIndex = annotStr.length
        append(annotStr)
        addStyle(
            style = SpanStyle(
                color = Color(0xff64B5F6),
                textDecoration = TextDecoration.Underline
            ), start = startIndex, end = endIndex
        )
        addStringAnnotation(
            tag = "URL",
            annotation = "https://platform.openai.com/account/api-keys",
            start = startIndex,
            end = endIndex
        )
    }
    ClickableText(
        annotatedLinkString,
        modifier = Modifier.padding(15.dp, 10.dp),
        onClick = {
            annotatedLinkString
                .getStringAnnotations("URL", it, it)
                .firstOrNull()?.let { stringAnnotation ->
                    uriHandler.openUri(stringAnnotation.item)
                }
        }
    )
}

@Composable
fun APIKeyList(apiKeyViewModel: APIKeyViewModel) {
    val selectedOption = apiKeyViewModel.apiKey.observeAsState()
    val allAPIKey = apiKeyViewModel.allAPIKey.observeAsState(listOf())
    val showEditDialog = remember { mutableStateOf(false) }
    val selectedKeyToEdit = remember { mutableStateOf<APIKeyEntity?>(null) }

    Column {
        APICreationLink()
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            itemsIndexed(allAPIKey.value) { _, item ->
                Card(
                    modifier = Modifier
                        .padding(start = 15.dp, end = 15.dp, top = 2.dp, bottom = 2.dp)
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            apiKeyViewModel.chooseAPI(item)
                            selectedKeyToEdit.value = item
                            showEditDialog.value = true
                        },
                    colors = CardDefaults.cardColors(
                        containerColor =
                        if (item.APIKey == selectedOption.value?.APIKey) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.inverseOnSurface
                        }
                    ),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp),
                            text = "sk-**********" + item.APIKey.takeLast(4),
                            color =
                            if (item.APIKey == selectedOption.value?.APIKey) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )

                        if (allAPIKey.value.size > 1) {
                            IconButton(
                                modifier = Modifier
                                    .size(42.dp)
                                    .padding(3.dp),
                                onClick = { apiKeyViewModel.deleteAPI(item) }
                            ) {
                                Icon(Icons.Rounded.Delete, contentDescription = "delete api")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog.value && selectedKeyToEdit.value != null) {
        APIKeyEditDialog(showEditDialog, selectedKeyToEdit.value!!, apiKeyViewModel)
    }
}

@Composable
fun AddKeyButton(apiKeyViewModel: APIKeyViewModel) {

    val showDialog = remember { mutableStateOf(false) }
    val inputKey = remember { mutableStateOf("") }
    val inputBaseUrl = remember { mutableStateOf("https://api.openai.com/v1/") }
    val inputModel = remember { mutableStateOf("gpt-3.5-turbo") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 20.dp, end = 20.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = {
                showDialog.value = true
            },
        ) {
            Icon(Icons.Filled.Add, "add new key")
        }
    }

    val confirmAction = {
        if (inputKey.value != "" && inputKey.value.startsWith("sk-")) {
            apiKeyViewModel.addAPI(inputKey.value, inputBaseUrl.value, inputModel.value)
            inputKey.value = ""
            inputBaseUrl.value = "https://api.openai.com/v1/chat/completions"
            inputModel.value = "gpt-3.5-turbo"
            showDialog.value = false
        }
    }

    val dismissAction = {
        inputKey.value = ""
        inputBaseUrl.value = "https://api.openai.com/v1/"
        inputModel.value = "gpt-3.5-turbo"
    }

    if (showDialog.value) {
        val titleContent: @Composable () -> Unit = {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, bottom = 20.dp)
                    .height(70.dp),
                painter = painterResource(id = R.drawable.key),
                contentDescription = "key_icon",
            )
        }
        APIKeyEditor(
            showDialog = showDialog,
            apiKey = inputKey,
            baseUrl = inputBaseUrl,
            model = inputModel,
            title = titleContent,
            confirmAction = confirmAction,
            dismissAction = dismissAction
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun APIKeyEditor(
    showDialog: MutableState<Boolean>,
    apiKey: MutableState<String>,
    baseUrl: MutableState<String>,
    model: MutableState<String>,
    title: @Composable () -> Unit,
    confirmAction: () -> Unit,
    dismissAction: () -> Unit = {},
) {
    NoPaddingAlertDialog(
        title = title,
        text = {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    value = apiKey.value,
                    onValueChange = { apiKey.value = it },
                    label = { Text(stringResource(R.string.api_key)) },
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    value = baseUrl.value,
                    onValueChange = { baseUrl.value = it },
                    label = { Text("Base URL") },
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    value = model.value,
                    onValueChange = { model.value = it },
                    label = { Text("Model") },
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            }
        },
        confirmButton = {
            TextButton(
                modifier = Modifier.padding(all = 3.dp),
                onClick = {
                    confirmAction()
                }
            )
            {
                Text(
                    text = stringResource(R.string.ok),
                    modifier = Modifier.padding(start = 30.dp, end = 30.dp)
                )
            }
        },
        dismissButton = {
            TextButton(
                modifier = Modifier.padding(all = 3.dp),
                onClick = {
                    dismissAction()
                    showDialog.value = false
                }
            )
            {
                Text(
                    text = stringResource(R.string.cancel),
                    modifier = Modifier.padding(start = 30.dp, end = 30.dp)
                )
            }
        }
    )
}

@Composable
fun APIKeyEditDialog(
    showDialog: MutableState<Boolean>,
    apiKeyEntity: APIKeyEntity,
    apiKeyViewModel: APIKeyViewModel
) {
    val apiKey = remember { mutableStateOf(apiKeyEntity.APIKey) }
    val baseUrl = remember { mutableStateOf(apiKeyEntity.baseUrl) }
    val model = remember { mutableStateOf(apiKeyEntity.model) }

    val confirmAction = {
        val updatedEntity = apiKeyEntity.copy(
            APIKey = apiKey.value,
            baseUrl = baseUrl.value,
            model = model.value
        )
        apiKeyViewModel.updateAPI(updatedEntity)
        showDialog.value = false
    }

    APIKeyEditor(
        showDialog = showDialog,
        apiKey = apiKey,
        baseUrl = baseUrl,
        model = model,
        title = { Text(stringResource(R.string.model_configuration), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp)) },
        confirmAction = confirmAction,
        dismissAction = { showDialog.value = false }
    )
}
