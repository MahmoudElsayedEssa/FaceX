package com.example.facex.data.local.ml.tensorflow.delegation


sealed class DelegateType {
    data object CPU : DelegateType()
    data object GPU : DelegateType()
    data object NNAPI : DelegateType()
}
