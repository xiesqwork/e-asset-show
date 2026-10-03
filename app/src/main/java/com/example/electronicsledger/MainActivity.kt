package com.example.electronicsledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = ThemePreferences(this)
        updateSystemBars(preferences.load())
        setContent {
            var palette by remember { mutableStateOf(preferences.load()) }
            SideEffect { updateSystemBars(palette) }
            LedgerTheme(palette) {
                LedgerApp(selectedPalette = palette, onPaletteChange = {
                    preferences.save(it)
                    palette = it
                })
            }
        }
    }

    private fun updateSystemBars(palette: LedgerPalette) {
        val transparent = android.graphics.Color.TRANSPARENT
        val style = if (palette.isDark) SystemBarStyle.dark(transparent)
            else SystemBarStyle.light(transparent, transparent)
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
}

@Composable
private fun rememberToday(): LocalDate {
    var today by remember { mutableStateOf(LocalDate.now()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                today = LocalDate.now()
                delay(1_000)
            }
        }
    }
    return today
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerApp(
    model: LedgerViewModel = viewModel(),
    selectedPalette: LedgerPalette = LedgerPalette.INDIGO,
    onPaletteChange: (LedgerPalette) -> Unit = {}
) {
    val state by model.state.collectAsStateWithLifecycle()
    val today = rememberToday()
    var editorId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedCategory by rememberSaveable { mutableStateOf("ALL") }
    var deleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showPalettePicker by rememberSaveable { mutableStateOf(false) }
    val editing = editorId != null
    val closeEditor = { editorId = null; model.clearError() }
    BackHandler(editing) { if (!state.saving) closeEditor() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (editing) if (editorId == 0L) "记录新产品" else "编辑产品" else stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (editing) IconButton(onClick = closeEditor, enabled = !state.saving) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "返回")
                    }
                },
                actions = {
                    if (!editing) IconButton(onClick = { showPalettePicker = true }) {
                        Icon(Icons.Outlined.Palette, "切换配色", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (!editing && !state.loading) ExtendedFloatingActionButton(
                onClick = { model.clearError(); editorId = 0L },
                icon = { Icon(Icons.Outlined.Add, null) }, text = { Text("添加产品") },
                containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            state.error?.let { error ->
                Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(error, Modifier.weight(1f))
                        TextButton(onClick = { if (editing) model.clearError() else model.reload() }, enabled = !state.saving) {
                            Text(if (editing) "知道了" else "重试")
                        }
                    }
                }
            }
            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (editing) {
                key(editorId) {
                    ProductEditor(
                        product = state.products.find { it.id == editorId }, today = today,
                        saving = state.saving, onSave = { model.save(it, closeEditor) }
                    )
                }
            } else {
                val shown = state.products.filter { selectedCategory == "ALL" || it.category.name == selectedCategory }
                val columns = productColumnCount(state.products.size)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.testTag("product-grid-$columns"),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 110.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text("让每一件喜欢，都用得值得。", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) { SummaryCard(state.products, today) }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("我的产品", fontWeight = FontWeight.Bold, fontSize = 21.sp, modifier = Modifier.weight(1f))
                            Text("${shown.size} 件", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item { FilterChip(selectedCategory == "ALL", onClick = { selectedCategory = "ALL" }, label = { Text("全部") }) }
                            items(Category.entries) { category ->
                                FilterChip(selectedCategory == category.name, onClick = { selectedCategory = category.name }, label = { Text(category.label) })
                            }
                        }
                    }
                    if (shown.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(Modifier.fillMaxWidth().padding(vertical = 38.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Devices, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(54.dp))
                            Text(if (state.products.isEmpty()) "记录你的第一件电子产品" else "这个分类还没有产品", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 18.dp))
                            Text("从一次购买，到每一天的陪伴", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                    items(shown, key = { it.id }) { product ->
                        ProductCard(product, today, compact = columns > 1,
                            onEdit = { model.clearError(); editorId = product.id },
                            onDelete = { model.clearError(); deleteId = product.id })
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) { Text("日均成本 = 买入价格 ÷ 持有天数\n购买当天计为第 1 天，按本地日期自动更新。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 19.sp) }
                }
            }
        }
    }

    if (showPalettePicker) PaletteDialog(selectedPalette, onPaletteChange, onDismiss = { showPalettePicker = false })

    state.products.find { it.id == deleteId }?.let { product ->
        AlertDialog(
            onDismissRequest = { if (!state.saving) deleteId = null },
            title = { Text("删除这条记录？") },
            text = { Column {
                Text("“${product.name}”将从账本中移除，删除后无法恢复。")
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            } },
            confirmButton = { TextButton(onClick = { model.delete(product.id) { deleteId = null } }, enabled = !state.saving) { Text(if (state.saving) "删除中…" else "删除", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deleteId = null }, enabled = !state.saving) { Text("取消") } }
        )
    }
}

@Composable
private fun SummaryCard(products: List<Product>, today: LocalDate) {
    val colors = MaterialTheme.colorScheme
    Card(colors = CardDefaults.cardColors(containerColor = colors.primaryContainer, contentColor = colors.onPrimaryContainer), shape = RoundedCornerShape(26.dp)) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Text("累计投入", fontSize = 14.sp)
            Text("¥ ${priceText(products.sumOf { it.priceCents })}", fontSize = 34.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp, bottom = 22.dp))
            HorizontalDivider(color = colors.onPrimaryContainer.copy(alpha = .15f))
            Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${products.size} 件电子产品")
                Text("更新于 ${today.monthValue}/${today.dayOfMonth}")
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product, today: LocalDate, compact: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val categoryTint = MaterialTheme.colorScheme.primary
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.padding(if (compact) 8.dp else 18.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 0.dp)) {
                Box(Modifier.size(if (compact) 18.dp else 46.dp).background(categoryTint.copy(alpha = .10f), RoundedCornerShape(if (compact) 5.dp else 12.dp)), contentAlignment = Alignment.Center) {
                    Icon(when (product.category) {
                        Category.PHONE -> Icons.Outlined.Smartphone
                        Category.COMPUTER -> Icons.Outlined.Laptop
                        Category.PERIPHERAL -> Icons.Outlined.Headphones
                        Category.OTHER -> Icons.Outlined.DevicesOther
                    }, null, tint = categoryTint, modifier = Modifier.size(if (compact) 15.dp else 24.dp))
                }
                if (compact) Text(product.category.label, color = categoryTint, fontSize = 11.sp, lineHeight = 16.sp, maxLines = 1)
                }
                if (!compact) Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(product.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("${product.category.label} · ${product.purchasedOn}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
                Box {
                    IconButton(onClick = { menu = true }, modifier = Modifier.size(if (compact) 28.dp else 48.dp)) { Icon(Icons.Outlined.MoreHoriz, "${product.name}更多操作", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(if (compact) 18.dp else 24.dp)) }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("编辑") }, leadingIcon = { Icon(Icons.Outlined.Edit, null, tint = MaterialTheme.colorScheme.primary) }, onClick = { menu = false; onEdit() })
                        DropdownMenuItem(text = { Text("删除", color = MaterialTheme.colorScheme.error) }, leadingIcon = { Icon(Icons.Outlined.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) }, onClick = { menu = false; onDelete() })
                    }
                }
            }
            if (compact) {
                Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
            }
            HorizontalDivider(Modifier.padding(vertical = if (compact) 8.dp else 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            if (compact) {
                Text("日均 / 元", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, lineHeight = 16.sp)
                BasicText(product.dailyCost(today).toPlainString(),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp), maxLines = 1,
                    style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, lineHeight = 26.sp),
                    autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 22.sp))
                BasicText("买入 ¥ ${priceText(product.priceCents)}", modifier = Modifier.fillMaxWidth().padding(top = 6.dp), maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp),
                    autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 12.sp))
                Text("${product.daysOwned(today)} 天", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 2.dp))
            } else FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    Text("日均使用成本", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text("¥ ${product.dailyCost(today).toPlainString()} / 天", color = MaterialTheme.colorScheme.primary, fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("买入 ¥ ${priceText(product.priceCents)}", fontSize = 13.sp)
                    Text("已持有 ${product.daysOwned(today)} 天", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 5.dp))
                }
            }
        }
    }
}

@Composable
private fun ProductEditor(product: Product?, today: LocalDate, saving: Boolean, onSave: (Product) -> Unit) {
    var name by rememberSaveable { mutableStateOf(product?.name ?: "") }
    var price by rememberSaveable { mutableStateOf(product?.let { priceText(it.priceCents) } ?: "") }
    var category by rememberSaveable { mutableStateOf(product?.category?.name ?: Category.PHONE.name) }
    var date by rememberSaveable { mutableStateOf((product?.purchasedOn ?: today).toString()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val selectedDate = LocalDate.parse(date)
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("记下买入价格，剩下的交给时间。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(name, onValueChange = { name = it; error = null }, label = { Text("产品名称") }, placeholder = { Text("例如：MacBook Air、无线耳机") }, singleLine = true, enabled = !saving, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        OutlinedTextField(price, onValueChange = { price = it; error = null }, label = { Text("买入价格") }, prefix = { Text("¥ ") }, placeholder = { Text("0.00") }, singleLine = true, enabled = !saving, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), supportingText = { Text("人民币，最多两位小数；允许 0 元") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Text("产品类型", fontWeight = FontWeight.Bold)
        LazyRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Category.entries) { item ->
                FilterChip(selected = category == item.name, onClick = { category = item.name }, enabled = !saving, label = { Text(item.label) })
            }
        }
        OutlinedCard(onClick = { showDatePicker = true }, enabled = !saving, shape = RoundedCornerShape(16.dp)) {
            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("买入日期", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(date, modifier = Modifier.padding(top = 6.dp), fontSize = 18.sp)
                }
                Icon(Icons.Outlined.CalendarToday, "选择买入日期", tint = MaterialTheme.colorScheme.primary)
            }
        }
        val cents = parsePriceCents(price)
        if (cents != null && selectedDate <= today) {
            val preview = Product(name = name, priceCents = cents, category = Category.valueOf(category), purchasedOn = selectedDate)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("截至今天 · 已持有 ${preview.daysOwned(today)} 天", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 13.sp)
                    Text("日均 ¥ ${preview.dailyCost(today).toPlainString()}", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 23.sp, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = {
            error = validateProduct(name, price, selectedDate, LocalDate.now())
            if (error == null) onSave(Product(product?.id ?: 0, name.trim(), parsePriceCents(price)!!, Category.valueOf(category), selectedDate))
        }, enabled = !saving, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
            Text(if (saving) "保存中…" else "保存产品", fontSize = 16.sp)
        }
        Text("记录仅保存在这台设备上。卸载应用或清除应用数据会删除记录。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 24.dp))
    }
    if (showDatePicker) PurchaseDateDialog(initialDate = selectedDate, today = today,
        onDismiss = { showDatePicker = false },
        onConfirm = { date = it.toString(); error = null; showDatePicker = false })
}
