package com.salestrack.data.remote

import com.salestrack.domain.model.Sale
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

class FirebaseSalesDataSource {
    private val firestore = Firebase.firestore

    suspend fun uploadSale(businessId: String, sale: Sale) {
        firestore.collection("businesses")
            .document(businessId)
            .collection("sales")
            .document(sale.id)
            .set(sale)
    }

    // Add other remote methods like getSales stream etc.
}
