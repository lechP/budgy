package com.lpi.budgy.domain

class AssetNotFound(assetId: String) : RuntimeException("Asset with id $assetId not found")
class CurrencyNotFound(currencyId: String) : RuntimeException("Currency with id $currencyId not found")
class InstitutionNotFound(institutionId: String) : RuntimeException("Institution with id $institutionId not found")
class RiskLevelNotFound(riskLevelId: String) : RuntimeException("Risk level with id $riskLevelId not found")