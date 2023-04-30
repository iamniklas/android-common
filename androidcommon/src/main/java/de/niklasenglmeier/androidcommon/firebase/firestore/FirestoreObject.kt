package de.niklasenglmeier.androidcommon.firebase.firestore

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.Query
import java.lang.reflect.Modifier

/**
 * This abstract class contributes to the clearer handling of Firebase Firestore.
 * The 3 methods fetch, push and update control the loading, pushing and updating of the object's associated document in the collection specified as a parameter.
 * To load the saved documents from a collection, use the static methods fetchAll or fetchAllQueried.
 * IMPORTANT: At the time being, your derived class is required to implement a non-argument constructor if there is non already.
 * @param collection The collection parameter is the parent collection for the child class
 */
abstract class FirestoreObject(var collection: CollectionReference) {
    var documentId: String? = null

    /**
     * This method loads a single object based on its documentId.
     * This only works if the documentId is not null which means the object already exists in the cloud.
     * Therefore, you must call the push() method in advance or load the documents from the whole collection with fetchAll or fetchAllQueried methods
     *
     * @param onSuccess The callback with the id matching object from Firestore
     * @param onFailure The callback if something failed with the related exception
     */
    inline fun <reified T : FirestoreObject> fetch(createIfNotExisting: Boolean, crossinline onSuccess: (T) -> Unit, crossinline onFailure: (Exception) -> Unit) {
        if(documentId == null) {
            onFailure(java.lang.IllegalArgumentException("Unable to fetch object when document id is null"))
        } else {
            collection
                .document(documentId!!)
                .get()
                .addOnSuccessListener {
                        documentSnapshot ->
                    if(documentSnapshot != null && documentSnapshot.exists()) {
                        val obj = documentSnapshot.toObject(T::class.java)!!
                        onSuccess(obj)
                    } else if(createIfNotExisting) {
                        this.push(
                            {
                                onSuccess(it as T)
                            },
                            {
                                onFailure(it)
                            }
                        )
                    } else {
                        onFailure(IllegalStateException("No document has been found and no document shall be created."))
                    }
                }
                .addOnFailureListener { exception -> onFailure(exception) }
        }
    }

    /**
     * This method updates the object related firestore document.
     * This only works if the documentId is not null which means the object already exists in the cloud.
     * Therefore, you must call the push() method in advance or load the documents from the whole collection with fetchAll or fetchAllQueried methods
     *
     * @param onSuccess The callback with the id matching object from Firestore
     * @param onFailure The callback if something failed with the related exception
     */
    fun update(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if(documentId == null) {
            throw java.lang.IllegalArgumentException("Unable to update object when document id is null")
        }

        collection
            .document(documentId!!)
            .update(objectToMap())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    /**
     * This method creates a new document for its related object.
     * This only works if the documentId is null which means the object does not exist in the cloud yet.
     *
     * @param onSuccess The callback with the id matching object from Firestore
     * @param onFailure The callback if something failed with the related exception
     */
    fun push(onSuccess: (FirestoreObject) -> Unit, onFailure: (Exception) -> Unit) {
        if(documentId != null) {
            collection
                .document(documentId!!)
                .set(objectToMap())
                .addOnSuccessListener { onSuccess(this) }
                .addOnFailureListener { onFailure(it) }
        } else {
            collection
                .add(objectToMap())
                .addOnSuccessListener { documentReference -> onSuccess(this.apply { documentId = documentReference.id }) }
                .addOnFailureListener { exception -> onFailure(exception) }
        }
    }

    /**
     * A simple helper method to convert any object to a map.
     * It is configured to handle specifically Firestore objects, so static members and fields which are annotated with @Exclude (Firestore Annotations) are ignored
     */
    fun objectToMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        javaClass.declaredFields.filter { !Modifier.isStatic(it.modifiers) && !it.isAnnotationPresent(
            Exclude::class.java) }.forEach {
            it.isAccessible = true
            map[it.name] = it.get(this@FirestoreObject)
        }
        return map
    }

    companion object {
        /**
         * This method loads all documents that match a certain condition for a particular class.
         * It automatically transforms the documents to the correct type, which is also determined by the cls parameter.
         *
         * @param query The query used to filter the documents from a collection
         * @param onSuccess Callback that contains a list of found objects
         * @param onFailure Callback if something failed containing the related exception
         */
        inline fun <reified T : FirestoreObject> fetchAllQueried(query: Query,
                                                                 crossinline onSuccess: (List<T>) -> Unit,
                                                                 crossinline onFailure: (Exception) -> Unit) {
            query
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val ids = querySnapshot.map { it.id }
                    val objects = querySnapshot.toObjects(T::class.java)
                    objects.forEachIndexed { index, t -> t.documentId = ids[index] }
                    onSuccess(objects)
                }
                .addOnFailureListener { exception -> onFailure(exception) }
        }

        /**
         * This method loads all documents for a particular class which is handed over by the cls parameter.
         * It automatically transforms the documents to the correct type, which is also determined by the cls parameter.
         *
         * @param cls The class that is used to determine the collection and list type in the callback
         * @param onSuccess Callback that contains a list of found objects
         * @param onFailure Callback if something failed containing the related exception
         */
        inline fun <reified T : FirestoreObject> fetchAll(cls: Class<T>,
                                                          crossinline onSuccess: (List<T>) -> Unit,
                                                          crossinline onFailure: (Exception) -> Unit) {
            val collection = cls.newInstance().collection

            collection
                .get()
                .addOnSuccessListener { querySnapshot ->
                    try {
                        val ids = querySnapshot.map { it.id }
                        val objects = querySnapshot.toObjects(T::class.java)
                        objects.forEachIndexed { index, t -> t.documentId = ids[index] }
                        onSuccess(objects)
                    } catch (e: java.lang.Exception) {
                        onFailure(e)
                    }
                }
                .addOnFailureListener { exception -> onFailure(exception) }
        }

        inline fun <reified T: FirestoreObject> fetchFirst(query: Query,
                                                           crossinline onSuccess: (T) -> Unit,
                                                           crossinline onFailure: (Exception) -> Unit
        ) {

        }

        inline fun <reified T: FirestoreObject> documentExists(query: Query,
                                                               crossinline onSuccess: (Boolean) -> Unit,
                                                               crossinline onFailure: (Exception) -> Unit) {

        }
    }
}