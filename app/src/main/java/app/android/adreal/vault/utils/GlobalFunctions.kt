package app.android.adreal.vault.utils

import app.android.adreal.vault.sharedpreferences.SharedPreferences

class GlobalFunctions {

    fun getNextPrimaryKey(): Int {
        SharedPreferences.write(Constants.PRIMARY_KEY, getCurrentPrimaryKey() + 1)
        return getCurrentPrimaryKey()
    }

    private fun getCurrentPrimaryKey(): Int {
        return SharedPreferences.read(Constants.PRIMARY_KEY, -1)
    }

    fun setCurrentPrimaryKey(primaryKey: Int) {
        SharedPreferences.write(Constants.PRIMARY_KEY, primaryKey)
    }
}