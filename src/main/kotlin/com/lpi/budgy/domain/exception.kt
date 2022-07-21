package com.lpi.budgy.domain

class InstitutionNotFound(institutionId: String) : RuntimeException("Institution with id $institutionId not found")