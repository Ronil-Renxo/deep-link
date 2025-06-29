package org.renxo.deeplinkapplication.navigation

import kotlinx.serialization.Serializable


interface NavRouts

sealed class AppRoutes {
    @Serializable
    data object SplashPage : NavRouts

    @Serializable
    data object ScanningPage : NavRouts
    @Serializable
    data class EditDataPage(val data:String) : NavRouts

    @Serializable
    data class RegisterPage(val session:String) : NavRouts
    @Serializable
    data object SelectionPage : NavRouts
    @Serializable
    data object EditPage : NavRouts

    @Serializable
    data object ShowMyVisitingCardPage : NavRouts
    @Serializable
    data class OtherUserPage(val contact_id: String, val templateId: Int?) : NavRouts

    @Serializable
    data class DeepLinkPage(val id: String,val templateId: String?=null) : NavRouts


}