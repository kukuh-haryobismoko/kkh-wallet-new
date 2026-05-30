package com.kkh.wallet.data.mapper

import com.kkh.wallet.data.local.entity.BudgetEntity
import com.kkh.wallet.data.local.entity.CategoryEntity
import com.kkh.wallet.data.local.entity.TransactionEntity
import com.kkh.wallet.data.local.entity.WalletEntity
import com.kkh.wallet.domain.model.Budget
import com.kkh.wallet.domain.model.Category
import com.kkh.wallet.domain.model.Transaction
import com.kkh.wallet.domain.model.TransactionType
import com.kkh.wallet.domain.model.Wallet
import com.kkh.wallet.domain.model.WalletType

fun WalletEntity.toDomain(): Wallet = Wallet(
    id = id,
    name = name,
    type = WalletType.fromName(type),
    providerName = providerName,
    iconKey = iconKey,
    colorHex = colorHex,
    currentBalance = currentBalance,
    creditLimit = creditLimit,
    usedLimit = usedLimit,
    billingDate = billingDate,
    dueDate = dueDate,
    notes = notes,
    isArchived = isArchived,
    createdAt = createdAt
)

fun Wallet.toEntity(): WalletEntity = WalletEntity(
    id = id,
    name = name,
    type = type.name,
    providerName = providerName,
    iconKey = iconKey,
    colorHex = colorHex,
    currentBalance = currentBalance,
    creditLimit = creditLimit,
    usedLimit = usedLimit,
    billingDate = billingDate,
    dueDate = dueDate,
    notes = notes,
    isArchived = isArchived,
    createdAt = createdAt
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    walletId = walletId,
    destinationWalletId = destinationWalletId,
    categoryId = categoryId,
    amount = amount,
    type = TransactionType.fromName(type),
    description = description,
    merchant = merchant,
    dateMillis = dateMillis,
    attachmentUri = attachmentUri,
    tags = tags,
    createdAt = createdAt
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    walletId = walletId,
    destinationWalletId = destinationWalletId,
    categoryId = categoryId,
    amount = amount,
    type = type.name,
    description = description,
    merchant = merchant,
    dateMillis = dateMillis,
    attachmentUri = attachmentUri,
    tags = tags,
    createdAt = createdAt
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    iconKey = iconKey,
    colorHex = colorHex,
    isIncome = isIncome,
    isCustom = isCustom,
    isArchived = isArchived
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    iconKey = iconKey,
    colorHex = colorHex,
    isIncome = isIncome,
    isCustom = isCustom,
    isArchived = isArchived
)

fun BudgetEntity.toDomain(): Budget = Budget(
    id = id,
    categoryId = categoryId,
    monthlyAmount = monthlyAmount,
    warnThresholdPercent = warnThresholdPercent,
    createdAt = createdAt
)

fun Budget.toEntity(): BudgetEntity = BudgetEntity(
    id = id,
    categoryId = categoryId,
    monthlyAmount = monthlyAmount,
    warnThresholdPercent = warnThresholdPercent,
    createdAt = createdAt
)
