package com.cnunez.docufast.admin.group.edit.presenter

import com.cnunez.docufast.admin.group.edit.contract.ListContract

class ListPresenter(
    private val view: ListContract.View,
    private val model: ListContract.Model
) : ListContract.Presenter {

    override fun loadGroups(){
        model.fetchGroups{groups,error->
            if(error == null){
                view.showGroups(groups!!)
                }
            else{
                view.showError(error)
            }

        }
    }
}