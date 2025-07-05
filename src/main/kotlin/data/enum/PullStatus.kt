package com.dpv.data.enum

enum class GithubState {
    OPEN,
    CLOSED;

    companion object {
        fun fromString(value: String): GithubState {
            return entries.find { it.name.equals(value, ignoreCase = true) }!!
        }
    }
}