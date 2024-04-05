package app.android.adreal.vault.utils

import app.android.adreal.vault.sharedpreferences.SharedPreferences

class GlobalFunctions {

    fun getNextPrimaryKey(): Int {
        return SharedPreferences.read(Constants.PRIMARY_KEY, getCurrentPrimaryKey().toString().toInt()) + 1
    }

    private fun getCurrentPrimaryKey() : Int{
        return SharedPreferences.read(Constants.PRIMARY_KEY, -1)
    }
}