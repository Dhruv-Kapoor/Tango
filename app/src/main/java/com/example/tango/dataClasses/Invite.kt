package com.example.tango.dataClasses

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

object INVITE_TYPES {
    const val TIC_TAC_TOE = 1
}

object INVITE_STATUS {
    const val INVITED = 1
    const val ACCEPTED = 2
    const val DECLINED = 3
}

data class Invite (
    var id: String = "",
    var invitedBy: String = "",
    var invitedByName: String = "",
    var invitedTo: String = "",
    var invitedToFcmToken: String = "",
    var invitedOn: Timestamp? = null,
    var inviteType: Int? = null,
    var status: Int? = null,
    var route: String = "",
    var roomNumber: String = "",
    var expiresAt: Timestamp? = null,

    @get:Exclude
    @set:Exclude
    var message: String = "",
)
