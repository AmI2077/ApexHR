package com.example.apexhr.features.shop.data

import com.example.apexhr.features.shop.data.dto.CategoryDto
import com.example.apexhr.features.shop.data.dto.ProductDto
import com.example.apexhr.features.shop.domain.Category
import com.example.apexhr.features.shop.domain.Product

fun CategoryDto.toDomain(): Category = Category(
    id = id,
    name = name.trimToNull(),
)

fun ProductDto.toDomain(): Product = Product(
    id = id,
    categoryId = categoryId,
    name = name.trimToNull(),
    description = description.trimToNull(),
    price = price,
    imageUrl = imageUrl.trimToNull(),
    stock = stock,
)

private fun String?.trimToNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
