package org.renxo.deeplinkapplication.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.renxo.deeplinkapplication.models.Contact
import org.renxo.deeplinkapplication.models.GenerateTokenRequest
import org.renxo.deeplinkapplication.models.GenerateTokenResponse
import org.renxo.deeplinkapplication.models.ParamModel
import org.renxo.deeplinkapplication.models.ResponseModel
import org.renxo.deeplinkapplication.models.User
import org.renxo.deeplinkapplication.networking.ApiRepository
import org.renxo.deeplinkapplication.networking.NetworkCallback
import org.renxo.deeplinkapplication.utils.AppConstants
import org.renxo.deeplinkapplication.utils.json
import org.renxo.deeplinkapplication.utils.preferenceManager
import javax.inject.Inject

@HiltViewModel
class MainVM @Inject constructor(private val repository: ApiRepository) :
    BaseViewModel() {
    var authToken: String? by mutableStateOf(null)
        private set

    var qrCode by mutableStateOf<String?>(null)
    var user by mutableStateOf<User?>(null)
    var contact by mutableStateOf<Contact?>(null)
    var sessionId :String?= ""

    init {
        checkNeedForFetchingDetails()
        viewModelScope.launch {
            sessionId=preferenceManager.getSessionId()
            preferenceManager.getContact()?.let {
                if (contact == null) {
                    contact = it
                }
            }
            preferenceManager.getQrCode()?.let {
                if (qrCode == null) {
                    qrCode = it
                }
            }
        }
    }


    fun checkNeedForFetchingDetails() {

        viewModelScope.launch {
            preferenceManager.getAuthToken().let {
                if (it.isNullOrEmpty()) {
                    preferenceManager.getSessionId()?.let { getToken(it) }
                } else {
                    getContactDetails()
                    authToken = it
                }
            }
        }

    }

    private val tokenCall by lazy { CallingHelper<GenerateTokenResponse>() }
    private fun getToken(id: String) {
        tokenCall.launchCall(
            call = {
                repository.getTokenUsingSessionID(
                    GenerateTokenRequest(
                        action="auth",
                        id,
                    )
                )
            },
            callback = object : NetworkCallback<GenerateTokenResponse> {
                override fun noInternetAvailable() {
                    viewModelScope.launch {
//                        _errorMessage.value = "Please Check your Internet Connection"
                    }
                }

                override fun unKnownErrorFound(error: String) {
                    viewModelScope.launch {
                        Log.e("unKnownErrorFound", ": $error")
                    }
                }

                override fun onProgressing(value: Boolean) {
                    // BaseViewModel already handles loading state
                }

                override fun onRequestAgainRestarted() {
                    // Optional: notify UI that request is being retried
                }

                override fun onSuccess(result: GenerateTokenResponse) {
                    result.token?.let {
                        viewModelScope.launch {
                            preferenceManager.setSessionId(it)
                            authToken = it
                            getContactDetails()
                        }
                    }
                }
            }
        )
    }


    private val detailCall by lazy { CallingHelper<ResponseModel>() }

    fun   getContactDetails() {

        detailCall.launchCall(
            call = {
                repository.getDetail(
                    ParamModel(action = "GetInfo"), authToken
                )
            },
            callback = object : NetworkCallback<ResponseModel> {
                override fun noInternetAvailable() {
                    viewModelScope.launch {
//                        _errorMessage.value = "Please Check your Internet Connection"
                    }
                }

                override fun unKnownErrorFound(error: String) {
                    Log.e("unKnownErrorFound", ": $error")

                    viewModelScope.launch {
//                        _errorMessage.value = error
                    }
                }

                override fun onProgressing(value: Boolean) {
                    // BaseViewModel already handles loading state
                }

                override fun onRequestAgainRestarted() {
                    // Optional: notify UI that request is being retried
                }

                override fun onSuccess(result: ResponseModel) {
                    if (result.result?.code == AppConstants.SuccessCodes.SUCCESS200) {
                        result.params?.let { params ->
                            qrCode = params[AppConstants.Params.url]


                            params[AppConstants.Params.contacts]?.let {
                                json.decodeFromString<List<Contact>>(
                                    it
                                ).firstOrNull()?.let { con ->
                                    contact = con
                                    viewModelScope.launch {
                                        contact?.let {
                                            preferenceManager.setData(it, qrCode)
                                        }
                                    }
                                }
                            }
                            params[AppConstants.Params.user]?.let {
                                json.decodeFromString<User>(
                                    it
                                ).let { us ->
                                    user = us
                                }
                            }
                        }
                    }


                }
            }
        )
    }


}


