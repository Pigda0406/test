# AI Code Review（ai_review → HEAD）

## 區塊 1
以下是針對本次 diff 的具體可執行建議，依照嚴重度、檔案與行號、問題說明與修正片段整理：

---

### 1. [Med] app/src/main/java/com/pxmart/android/di/mvvm/view/homeFunction/GridDragCallback.kt
- **問題說明**：RecyclerView 拖曳時啟用的抖動動畫使用硬體加速圖層（LAYER_TYPE_HARDWARE），但未在動畫結束時清理圖層，可能導致記憶體與繪製效能問題。
- **建議修正**：在動畫取消時，除了取消動畫，也要呼叫 `setLayerType(View.LAYER_TYPE_NONE, null)`，確保釋放硬體加速圖層。
- **修正片段**：
```kotlin
private fun stopAllJiggles(rv: RecyclerView) {
    rv.children.forEach { child ->
        (child.getTag(R.id.tag_jiggle_animator) as? ObjectAnimator)?.let { anim ->
            anim.cancel()
            child.setTag(R.id.tag_jiggle_animator, null)
            child.setLayerType(View.LAYER_TYPE_NONE, null) // 新增
        }
        child.rotation = 0f
    }
}
```

---

### 2. [Med] app/src/main/java/com/pxmart/android/di/mvvm/view/homeFunction/HomeFunctionDragGridAdapter.kt
- **問題說明**：Adapter 使用 `setHasStableIds(true)`，但 `getItemId` 回傳的空格 ID 為 `Long.MIN_VALUE + position`，可能與其他 item ID 衝突，建議改用不會與有效 ID 衝突的範圍。
- **建議修正**：改用負數且與有效 ID 不重疊的範圍，例如 `-position.toLong() - 1`。
- **修正片段**：
```kotlin
override fun getItemId(position: Int): Long = slots[position]?.toLong() ?: (-position.toLong() - 1)
```

---

### 3. [Med] app/src/main/java/com/pxmart/android/di/mvvm/view/user/msg/MessageDetailActivity.kt 及 app/src/main/java/com/pxmart/android/di/mvvm/view/careMode/CareModeMessageDetailActivity.kt
- **問題說明**：URLSpan 替換為自訂 `CustomClickEventURLSpan`，但判斷 `view.text` 是否為 `SpannableString` 不夠嚴謹，應改用 `Spannable` 介面判斷，避免因非 SpannableString 導致不替換。
- **建議修正**：
```kotlin
private fun setAndReplaceUrlSpansWithCustomUrlSpan(
    view: TextView,
    predicate: (URLSpan) -> Boolean,
    newSpanProvider: (url: String) -> CustomClickEventURLSpan
) {
    val text = view.text
    if (text !is Spannable) {
        return
    }
    val spannable = text
    // 以下不變
}
```

---

### 4. [Med] app/src/main/java/com/pxmart/android/di/mvvm/view/user/HomeFunctionEditActivity.kt
- **問題說明**：RecyclerView 使用 GridLayoutManager 且有拖曳功能，建議在 `onDestroy` 或 Activity 結束時，解除 ItemTouchHelper 附著，避免記憶體洩漏。
- **建議修正**：
```kotlin
override fun onDestroy() {
    super.onDestroy()
    itemTouchHelper.attachToRecyclerView(null)
}
```

---

### 5. [Low] app/src/main/java/com/pxmart/android/di/base/PXPayBaseActivity.kt
- **問題說明**：`isEdgeToEdge()` 預設為 false，建議改為 open 並在子類別覆寫，避免未設定導致 UI 不一致。
- **建議修正**：
```kotlin
protected open fun isEdgeToEdge(): Boolean = false
// 子類別覆寫為 true
```
（此點已有實作，確認子類別有覆寫）

---

### 6. [Low] app/src/main/java/com/pxmart/android/di/mvvm/view/user/main_fragments/home/HomeFragment.kt
- **問題說明**：多處使用 `ViewCompat.setOnApplyWindowInsetsListener`，建議在 `doOnAttach` 觸發時呼叫 `requestApplyInsets()`，確保 Insets 正確套用。
- **建議修正**：
```kotlin
binding.clSearchProductBar.doOnAttach { ViewCompat.requestApplyInsets(binding.clSearchProductBar) }
```
（此點已修正）

---

### 7. [Low] app/build.gradle
- **問題說明**：多處重複定義相同 `buildConfigField`，建議整理避免重複，減少 buildConfig 膨脹。
- **建議修正**：合併相同變數定義，或使用 flavor 變數統一管理。

---

### 8. [Med] 權限/隱私與 WebView 安全
- **問題說明**：`CommonWebActivity` 與 `WebApplicationActivity` 使用外部 URL，建議檢查 URL 是否安全，避免開啟惡意連結。
- **建議修正**：在啟動 WebView 前，加入 URL 白名單或使用 `LinkChecker` 類似工具過濾。

---

### 9. [Med] Coroutine/Flow 使用
- **問題說明**：`MessageDetailActivity` 與 `CareModeMessageDetailActivity` 中，`readMessageFlow` 被註解掉，可能導致已讀狀態未即時更新。
- **建議修正**：確認是否有其他機制替代，若無，建議恢復觀察並處理 UI 更新。

---

### 10. [Med] RecyclerView DiffUtil / Adapter 效能
- **問題說明**：新加入多個 RecyclerView Adapter（HomeFunctionDragGridAdapter、HomeFunctionGridAdapter、BalancePointAdapter）均未使用 DiffUtil，直接使用 notifyDataSetChanged，可能導致效能下降。
- **建議修正**：改用 DiffUtil 計算差異，提升 RecyclerView 更新效能。

---

### 11. [Low] Proguard/R8 風險
- **問題說明**：新增多個 data class 使用 Gson 與 kotlinx.serialization，請確認 Proguard 規則允許序列化類別不被混淆。
- **建議修正**：新增 Proguard 規則：
```proguard
-keep class com.pxmart.android.bean.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
```

---

## 回歸測試清單
- HomeFunctionEditActivity 拖曳功能正常，拖曳結束後資料正確保存。
- MessageDetailActivity 與 CareModeMessageDetailActivity 顯示訊息與 URL 點擊行為正常。
- HomeFragment UI Insets 正確，無遮擋。
- WebView 開啟外部連結安全，無惡意跳轉。
- RecyclerView 更新時無閃爍或異常。
- PXPayPlus SDK 版本升級後支付流程正常。
- AndroidManifest 新增 Activity `HomeFunctionEditActivity` 正常啟動。

---

## 需要補充上下文
- Android Gradle Plugin 版本與 targetSdkVersion，確認 WindowInsets API 使用是否最佳。
- Proguard/R8 設定檔，確認序列化類別混淆規則。
- Coroutine Flow 觀察邏輯是否有其他替代方案。
- WebView 相關設定與權限，確認安全性。

---

以上建議請依專案優先度與風險評估逐步導入。

## 區塊 2
以下是針對本次 diff 的具體可執行審查建議：

---

### 1. BannerAdapter.kt

- **嚴重度**: Med  
- **檔名:行號**: BannerAdapter.kt:23, 39, 41, 51, 57, 65  
- **問題說明**:  
  - `horizontalPadding` 固定為 20，建議改為可配置或從資源取得，避免硬編碼。  
  - `dpTo2Px` 轉換後使用 `.toInt()`，可能導致精度損失，建議使用 `roundToInt()`。  
  - 註解掉的 `setRoundCorner`、`setPageMargin` 可能影響 UI，需確認是否為預期。  
  - `pageIndicator` 函式使用 `Locale.getDefault(Locale.Category.FORMAT)`，建議改用 `Locale.getDefault()` 或明確指定 `Locale.US` 以避免格式差異。  
  - `binding.tvPageIndicator` 動態設定 margin，建議確認 Compose 或 View 的重繪是否會導致 margin 重複累加。  
- **修正片段**:
```kotlin
private val horizontalPadding = context.resources.getDimensionPixelSize(R.dimen.banner_horizontal_padding)

val horizontalPaddingPx = horizontalPadding.roundToInt()

// pageIndicator 函式
private fun pageIndicator(page: Int, pageSize: Int): String {
    return String.format(Locale.getDefault(), "%d/%d", page, pageSize)
}
```

---

### 2. HeaderAdapter.kt

- **嚴重度**: Low  
- **檔名:行號**: HeaderAdapter.kt:74  
- **問題說明**:  
  - `messageBadge.text = String.format(messageCount.toString())` 無意義，直接賦值即可。  
  - 建議使用 `context.getString(R.string.message_max_count)` 以外的字串格式化方式，避免硬編碼。  
- **修正片段**:
```kotlin
if (messageCount > 99) {
    messageBadge.text = context.getString(R.string.message_max_count)
} else {
    messageBadge.text = messageCount.toString()
}
```

---

### 3. HomeFunctionAdapter.kt & HomeFunctionItemAdapter.kt

- **嚴重度**: Med  
- **檔名:行號**: HomeFunctionAdapter.kt 全檔, HomeFunctionItemAdapter.kt 全檔  
- **問題說明**:  
  - `notifyDataSetChanged()` 使用過多，建議改用 DiffUtil 以提升 RecyclerView 效能與避免 UI 重繪過度。  
  - `onHolder` 中每次都新建 Adapter，可能導致 RecyclerView 重複初始化，建議將 Adapter 設為 ViewHolder 屬性並重用。  
  - `getItemViewType` 及 `getItemCount` 實作中，`list.size` 位置可能越界，建議加強邊界檢查。  
- **修正片段**:
```kotlin
// HomeFunctionAdapter.kt
private val diffCallback = object : DiffUtil.ItemCallback<UserFunctionEntry>() {
    override fun areItemsTheSame(oldItem: UserFunctionEntry, newItem: UserFunctionEntry) = oldItem.userId == newItem.userId
    override fun areContentsTheSame(oldItem: UserFunctionEntry, newItem: UserFunctionEntry) = oldItem == newItem
}

fun updateData(newList: List<UserFunctionEntry>) {
    val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
        override fun getOldListSize() = list.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            list[oldItemPosition].userId == newList[newItemPosition].userId
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            list[oldItemPosition] == newList[newItemPosition]
    })
    list = newList
    diffResult.dispatchUpdatesTo(this)
}

// HomeFunctionItemAdapter.kt
override fun onHolder(holder: AbsViewHolder, data: Int?, position: Int) {
    if (holder is ItemViewHolder) {
        data?.let {
            val item = homeFunctionFromCode(it)
            item?.let { item ->
                (holder.item as HomeFunctionCustomView).bind(item)
                holder.rootView.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    } else if (holder is EditViewHolder) {
        holder.rootView.setOnClickListener {
            onEditClick()
        }
    }
}

override fun getItemViewType(position: Int): Int {
    return if (position == list.size) typeEdit else typeItem
}

override fun getItemCount(): Int = list.size + 1
```

---

### 4. PromotionImageAdapter.kt

- **嚴重度**: Low  
- **檔名:行號**: PromotionImageAdapter.kt:14, 29  
- **問題說明**:  
  - `horizontalPadding` 參數可為 null，使用時需注意避免 NullPointerException。  
  - `updatePadding` 會覆蓋原有 padding，建議先取得原 padding 再加上新 padding。  
- **修正片段**:
```kotlin
horizontalPadding?.let {
    binding.root.apply {
        val padding = context.dpTo2Px(it).toInt()
        updatePadding(
            left = padding + paddingLeft,
            right = padding + paddingRight,
            top = paddingTop,
            bottom = paddingBottom
        )
    }
}
```

---

### 5. ThirdPartyAdapter.kt

- **嚴重度**: Low  
- **檔名:行號**: ThirdPartyAdapter.kt:22, 40, 45, 50, 56, 99, 127, 133  
- **問題說明**:  
  - `coffeeCups` 由 String 改為 Int，需確認外部調用是否同步更新。  
  - `visibility` 控制改為 `View.GONE`，建議統一使用 `isVisible` 或 `isGone`，提升可讀性。  
  - 變數命名不一致（`home_entrance` 改為 `homeEntranceV7340`），需確認後端資料結構是否同步。  
- **修正片段**:
```kotlin
private var coffeeCups: Int? = null

// 更新 visibility
when {
    it <= 0 -> binding.ivCupPlus.isVisible = false
    it in 1..99 -> {
        binding.ivCupPlus.isVisible = false
        binding.tvCups.text = it.toString()
    }
    else -> binding.ivCupPlus.isVisible = true
}
```

---

### 6. DataStoreManager.kt

- **嚴重度**: Med  
- **檔名:行號**: DataStoreManager.kt:469-505  
- **問題說明**:  
  - 使用 Gson 解析與序列化時，缺少錯誤處理與異常回退，建議加強。  
  - `loadUserFunctionList` 使用 `dataStore.data.first()`，可能在主線程造成阻塞，建議改用 Coroutine 並在 IO Dispatcher 執行。  
- **修正片段**:
```kotlin
private suspend fun loadUserFunctionList(): UserFunctionList = withContext(Dispatchers.IO) {
    val prefs = dataStore.data.first()
    val json = prefs[stringPreferencesKey(HOME_FUNCS_USERS_OBJECT)] ?: return@withContext UserFunctionList()
    return@withContext try {
        Gson().fromJson(json, userFuncListType)
    } catch (e: Exception) {
        UserFunctionList()
    }
}
```

---

### 7. LinkChecker.kt

- **嚴重度**: Low  
- **檔名:行號**: LinkChecker.kt:新增函式區段  
- **問題說明**:  
  - `extractParamsFromUrl` 兩層 Uri 解析邏輯良好，但建議加強單元測試覆蓋各種 fragment 與 query 組合。  
  - `equalsAny`、`startsWithAny` 擴展函式，建議加上 `ignoreCase` 參數預設值。  
- **修正片段**:
```kotlin
private fun String.equalsAny(vararg targets: String, ignoreCase: Boolean = false): Boolean {
    return targets.any { it.equals(this, ignoreCase = ignoreCase) }
}
```

---

### 8. RecyclerView GridEqualSpacingDecoration.kt

- **嚴重度**: Low  
- **檔名:行號**: GridEqualSpacingDecoration.kt:8-43  
- **問題說明**:  
  - 計算間距時使用浮點數 floor，避免累積誤差，良好實踐。  
  - 建議補充註解說明為何左右邊界為 0，方便後續維護。  
- **修正片段**: 無需修改，建議補充註解。

---

### 9. CustomClickEventURLSpan.kt

- **嚴重度**: Low  
- **檔名:行號**: CustomClickEventURLSpan.kt 全檔  
- **問題說明**:  
  - 自訂 URLSpan 用於攔截點擊事件，建議確認是否有防止多次點擊的機制。  
- **修正片段**:  
```kotlin
override fun onClick(widget: View) {
    if (widget.isClickable) {
        onCustomClick(url, widget)
    }
}
```

---

### 10. HomeFunctionCustomView.kt

- **嚴重度**: Low  
- **檔名:行號**: HomeFunctionCustomView.kt 全檔  
- **問題說明**:  
  - `bind` 函式中使用 `context.getColor`，需確認 minSdk 是否支援，否則改用 `ContextCompat.getColor`。  
- **修正片段**:
```kotlin
rootLayout.setCardBackgroundColor(
    if (homeFunctionType.isLock) ContextCompat.getColor(context, R.color.color_FFEAEC)
    else ContextCompat.getColor(context, R.color.color_EAEFFD)
)
```

---

## 回歸測試清單

1. BannerAdapter 頁碼顯示與 margin 是否符合 UI 設計。  
2. HeaderAdapter 訊息徽章顯示與數字格式。  
3. HomeFunctionAdapter 與 HomeFunctionItemAdapter 的列表更新與點擊事件。  
4. PromotionImageAdapter padding 是否正確。  
5. ThirdPartyAdapter coffeeCups 顯示邏輯與點擊事件。  
6. DataStoreManager 中用戶功能列表的存取與異常處理。  
7. LinkChecker URL 解析與判斷函式。  
8. RecyclerView GridEqualSpacingDecoration 的 item 間距。  
9. CustomClickEventURLSpan 點擊事件攔截。  
10. HomeFunctionCustomView UI 顯示與顏色。  

---

## 風險點與需補充資訊

- DataStoreManager 使用的 Coroutine Dispatcher 與調用上下文不明，需確認是否在非主線程執行。  
- BannerAdapter 與 PromotionImageAdapter padding 與 margin 設定是否與 Compose 或其他 UI 元件整合良好。  
- HomeFunctionAdapter 與 HomeFunctionItemAdapter 是否有其他外部調用影響 RecyclerView 行為。  
- 目標 SDK 版本與 AGP 版本，影響部分 API 使用（如 `context.getColor`）。  
- 需確認是否有 Proguard/R8 混淆規則影響 Gson 反序列化。  

---

以上建議請依專案實際情況調整，並搭配單元測試與 UI 測試確保品質。

## 區塊 3
以下為本次 diff 的具體可執行審查建議：

---

### 1. Low - app/src/main/res/drawable/image_payment_record.xml  
**問題說明**：  
- 修改了 vector drawable 的尺寸與 pathData，且刪除了部分 path group。  
- 需確認此修改是否會影響 UI 呈現，尤其是不同解析度下的顯示。  
- Vector drawable 較大時，可能影響 16KB page size 限制，建議檢查是否有過大資源。  

**建議修正**：  
- 確認 vector drawable 的尺寸與 pathData 是否符合設計稿。  
- 若 vector 複雜度高，考慮拆分或使用 PNG 以避免編譯時 R8/Proguard 影響。  
- 無需程式碼修正，僅需設計確認。

---

### 2. Med - app/src/main/res/layout/activity_home_function_edit.xml (新檔案)  
**問題說明**：  
- 使用 NestedScrollView 包裹 ConstraintLayout，內部有多個 RecyclerView。  
- RecyclerView 高度為 wrap_content，可能導致 NestedScrollView 與 RecyclerView 滾動衝突，造成效能問題或 ANR。  
- 生命週期與記憶體：多 RecyclerView 同時存在，需注意資源釋放與重複繪製。  
- 建議檢查 RecyclerView 是否有設定穩定 ID 與 DiffUtil，避免重繪過度。

**建議修正**：  
- RecyclerView 高度改為固定高度或使用 NestedScrollingEnabled = false，避免 NestedScrollView 與 RecyclerView 滾動衝突。  
- 確認 RecyclerView Adapter 有實作 DiffUtil 並啟用 setHasStableIds(true)。  
- 範例修正片段（Kotlin）：

```kotlin
recyclerView.apply {
    setHasFixedSize(true)
    isNestedScrollingEnabled = false
    adapter = yourAdapter.apply {
        setHasStableIds(true)
    }
}
```

---

### 3. Low - app/src/main/res/layout/activity_main.xml  
**問題說明**：  
- 將 MegaPxMartSelectorView 從 gone 改為 invisible，並調整 ConstraintLayout 約束。  
- invisible 保留空間，gone 不保留，需確認 UI 需求。  
- 無明顯生命週期或效能問題。

**建議修正**：  
- 確認 invisible 是否符合設計需求，若不需佔位，建議改回 gone。  
- 無需程式碼修正。

---

### 4. Med - app/src/main/res/layout/activity_map.xml  
**問題說明**：  
- 將原本 LinearLayout 包裹的 RecyclerView 改為直接使用 RecyclerView。  
- 移除多餘的 ViewGroup 有助於減少層級，提升效能。  
- 需確認 RecyclerView 是否有設定穩定 ID 與 DiffUtil。

**建議修正**：  
- 確認 RecyclerView Adapter 有實作 DiffUtil 並啟用 setHasStableIds(true)。  
- 無需 XML 修正。

---

### 5. Low - app/src/main/res/layout/item_home_balance.xml  
**問題說明**：  
- 大幅重構 layout，將多個 ConstraintLayout 與 CardView 合併，移除多餘層級。  
- 新增多個 Guideline 與 ImageView/TextView，提升可讀性與維護性。  
- 需確認新 layout 是否符合 16KB page size 限制。  
- 需確認 ImageView 是否有設定 contentDescription，提升無障礙。  

**建議修正**：  
- 新增 ImageView contentDescription 範例：

```xml
<androidx.appcompat.widget.AppCompatImageView
    android:id="@+id/iv_balance"
    android:contentDescription="@string/desc_balance_icon"
    ... />
```

- 確認是否有使用 DiffUtil 與穩定 ID 於相關 RecyclerView Adapter。  
- 無需其他程式碼修正。

---

### 6. Low - app/src/main/res/layout/item_home_banner.xml  
**問題說明**：  
- 將原本的 BlueIndictorView 改為 TextView 顯示頁碼指示。  
- 需確認此改動是否影響使用者體驗與無障礙。  

**建議修正**：  
- TextView 建議加上 contentDescription，方便無障礙使用者。  
- 範例：

```xml
<TextView
    android:id="@+id/tvPageIndicator"
    android:contentDescription="@string/page_indicator_desc"
    ... />
```

---

### 7. Low - app/src/main/res/layout/item_home_payment_member.xml  
**問題說明**：  
- 將 ImageView 改為 TextView 顯示餘額，並新增多個 TextView。  
- 需確認字串大小單位是否使用 sp，避免字體縮放問題。  
- TextView textSize 使用 dp，應改為 sp。

**建議修正**：  
- 將 textSize 從 dp 改為 sp：

```xml
<TextView
    android:id="@+id/tv_balance"
    android:textSize="20sp"
    ... />
```

- 其他 TextView 同理。

---

### 8. Low - 新增多個 layout 檔案 (home_function.xml, home_function_background_item.xml, home_function_edit_item.xml, home_function_item.xml, item_custom_home_function.xml, item_function.xml, item_grid_empty.xml, item_grid_function_add.xml, item_grid_function_draggable.xml)  
**問題說明**：  
- 多個新 layout，均為 RecyclerView item 或功能區塊。  
- 需確認 RecyclerView Adapter 是否有實作 DiffUtil 與 setHasStableIds(true)，避免重繪與滾動效能問題。  
- 需注意 item layout 大小是否符合 16KB page size 限制。  

**建議修正**：  
- Adapter 實作範例：

```kotlin
class YourAdapter : ListAdapter<YourData, YourViewHolder>(DIFF_CALLBACK) {
    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<YourData>() {
            override fun areItemsTheSame(oldItem: YourData, newItem: YourData) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: YourData, newItem: YourData) = oldItem == newItem
        }
    }
}
```

- item layout 建議審查是否有過多層級，避免過度嵌套。

---

## 回歸測試清單

1. **UI 顯示**  
   - 確認 image_payment_record.xml 在各解析度下顯示正常。  
   - activity_home_function_edit.xml 中多個 RecyclerView 滾動與顯示正常，無卡頓。  
   - activity_main.xml 中 MegaPxMartSelectorView 顯示與隱藏行為符合預期。  
   - activity_map.xml 中 rvStoreTypes 顯示與滾動正常。  
   - item_home_balance.xml 新版 UI 顯示正常，無重疊或錯位。  
   - item_home_banner.xml 頁碼指示顯示正常。  
   - item_home_payment_member.xml 餘額顯示字體大小與顏色正常。  
   - 新增的 home_function 相關 item 顯示與點擊事件正常。

2. **效能與記憶體**  
   - 多 RecyclerView 同時存在時，滾動流暢無 ANR。  
   - Adapter 使用 DiffUtil 與穩定 ID，避免重複重繪。  
   - 無記憶體洩漏。

3. **無障礙**  
   - ImageView 與重要 TextView 有適當 contentDescription。  
   - 字體大小使用 sp，支援系統字體縮放。

---

## 風險點

- Vector drawable 修改可能導致 UI 顯示異常。  
- 多 RecyclerView 嵌套 NestedScrollView 可能導致滾動衝突與效能問題。  
- TextView textSize 使用 dp 可能導致字體縮放異常。  
- Adapter 未使用 DiffUtil 與穩定 ID 可能導致 RecyclerView 重繪效能低下。  
- 新增 layout 未確認 16KB page size 限制，可能導致編譯錯誤。

---

## 需要補充上下文

- Android Gradle Plugin 版本與 targetSdkVersion，影響 vector drawable 與 RecyclerView 行為。  
- 相關 RecyclerView Adapter Kotlin/Java 實作碼，確認 DiffUtil 與 setHasStableIds 是否有實作。  
- Compose 是否有使用，因本次 diff 皆為 XML，無 Compose 相關。  
- Coroutine/Flow 使用情況，無相關程式碼變更。  
- 權限/隱私與 WebView 安全無相關變更。

---

以上建議請依專案實際需求與設計稿確認後執行。

## 區塊 4
以下是針對本次 diff 的具體可執行建議：

---

### 1. Medium - app/src/main/res/layout/item_home_promotion.xml:9-26  
**問題說明**：  
- 將 CardView 改為 ShapeableImageView，雖然可減少 View 層級，但需注意 ShapeableImageView 的 `scaleType="fitXY"` 可能導致圖片變形，且 `adjustViewBounds="true"` 與 `scaleType="fitXY"` 可能衝突。  
- `foreground="?android:attr/selectableItemBackground"` 在 ImageView 上可能不會有預期的點擊水波紋效果，建議改用可點擊的容器包裹。  

**建議修正片段**：
```xml
<com.google.android.material.imageview.ShapeableImageView
    android:id="@+id/iv_promotion_photo"
    android:layout_width="0dp"
    android:layout_height="0dp"
    android:scaleType="centerCrop"  <!-- 避免圖片變形 -->
    android:clickable="true"
    android:focusable="true"
    android:foreground="?attr/selectableItemBackground"  <!-- 使用 appcompat attr -->
    app:shapeAppearanceOverlay="@style/roundedCorner8"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent" />
```

---

### 2. Medium - app/src/main/res/layout/item_home_third_party.xml:全檔  
**問題說明**：  
- 將外層 LinearLayout 改為 ConstraintLayout，並大量使用 ConstraintLayout 的 dimensionRatio 及 0dp 寬高，提升效能與彈性，這是正向改進。  
- LottieAnimationView 的 `app:lottie_rawRes` 移除，改用程式控制載入，避免 layout inflate 時過度耗時。  
- LottieAnimationView 與 ImageView 重疊，需確保兩者 visibility 狀態管理正確，避免記憶體浪費。  
- CardView 的 `app:cardElevation="0dp"` 建議改用 MaterialCardView 並使用 elevation 屬性，提升 Material Design 一致性。  

**建議修正片段**（部分示意）：
```xml
<com.airbnb.lottie.LottieAnimationView
    android:id="@+id/imageRtNextDay"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/white"
    android:scaleType="fitXY"
    android:visibility="visible"
    app:lottie_autoPlay="false"
    app:lottie_loop="true"
    <!-- 移除 app:lottie_rawRes，改由程式載入 -->
/>
```

**程式碼中載入 Lottie 動畫示例**：
```kotlin
imageRtNextDay.setAnimation(R.raw.ic_next_day)
```

---

### 3. Low - app/src/main/res/layout/item_store.xml:184-200  
**問題說明**：  
- 原本 RecyclerView 被包在 LinearLayout 中，改為直接使用 RecyclerView 並用 ConstraintLayout 約束，減少 View 層級，有助於效能。  
- 但需確認 RecyclerView 的 Adapter 是否有設定 `setHasStableIds(true)` 並實作 `getItemId()`，避免 RecyclerView 項目重繪異常。  
- `android:layout_width="wrap_content"` 改為 `match_parent` 或 0dp + constraint，避免寬度不確定導致的測量問題。  

**建議修正片段**：
```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/rvStoreTypes"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:clipToPadding="false"
    android:overScrollMode="never"
    android:paddingVertical="12dp"
    android:scrollbars="none"
    app:layout_constraintTop_toBottomOf="@id/tv_parking"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent" />
```

**Adapter 設定示例**：
```kotlin
adapter.setHasStableIds(true)

override fun getItemId(position: Int): Long {
    return dataList[position].uniqueId
}
```

---

### 4. Low - app/src/main/res/layout/item_store_parking_location_cluster.xml:41  
**問題說明**：  
- 替換圖片資源名稱，需確認新資源 ic_circle_close_333333 是否符合設計規範與色彩一致性。  
- 無其他安全或效能疑慮。  

---

## 其他建議與風險點

- **Coroutine/Flow**：本次 diff 無 Kotlin 程式碼變更，無法評估 Coroutine/Flow 使用。  
- **Compose 副作用鍵**：無 Compose 相關變更。  
- **RecyclerView**：建議確認所有 RecyclerView Adapter 是否有設定穩定 ID，避免重繪異常。  
- **權限/隱私與 WebView 安全**：無 WebView 相關變更。  
- **DeepLink/Intent 安全**：無相關變更。  
- **ANR/效能**：Layout 減少層級與使用 ConstraintLayout dimensionRatio 有助於效能。Lottie 動畫建議程式控制載入，避免 inflate 時耗時。  
- **16KB page size 相容**：無 native code或大型資源變更，無影響。  
- **Proguard/R8 風險**：無程式碼變更，無影響。  
- **可測試性**：建議新增 UI 測試覆蓋新 Layout，特別是 RecyclerView 項目顯示與 Lottie 動畫狀態。

---

## 回歸測試清單

1. Promotion 項目圖片顯示是否正常，點擊效果是否有水波紋。  
2. Home Third Party 項目中 Lottie 動畫與 ImageView 切換是否正常，無閃爍或記憶體泄漏。  
3. Store 頁面 RecyclerView 是否正常顯示，滾動流暢，項目 ID 是否穩定。  
4. Store Parking Location Cluster 圖示是否正確顯示。  
5. 整體 UI 在不同螢幕尺寸與方向下是否正常。

---

## 需要補充的上下文

- Android Gradle Plugin 版本與 targetSdkVersion。  
- RecyclerView Adapter 相關 Kotlin 檔案，確認是否有設定穩定 ID。  
- Lottie 動畫載入與釋放邏輯程式碼。  
- 是否有 Compose 相關頁面或混用 Compose。  

---

如需更深入 Coroutine/Flow 或安全性建議，請提供相關 Kotlin 程式碼或 WebView 使用情境。

## 區塊 5
此 diff 內容為刪除一個非常大的 JSON 檔案（app/src/main/res/raw/ic_hours.json），該檔案內容為 Lottie 動畫的 JSON 資料。

---

### Code Review 建議

1. **嚴重度**: Low  
   **檔名: app/src/main/res/raw/ic_hours.json**  
   **問題說明**: 刪除 Lottie 動畫 JSON 檔案，需確認此動畫是否仍有被使用。若有使用但檔案被刪除，會導致動畫無法顯示，造成 UI 異常。  
   **建議修正**:  
   - 確認此檔案是否仍被引用於 Compose、View 或其他 UI 元件中。  
   - 若仍使用，請勿刪除或同步更新引用。  
   - 若確定不再使用，建議同時清理相關引用，避免殘留死碼。  

2. **嚴重度**: Med  
   **檔名: app/src/main/res/raw/ic_hours.json**  
   **問題說明**: Lottie JSON 檔案通常較大，刪除可減少 APK 體積，但需評估是否影響 App 生命週期中動畫的載入與釋放。  
   **建議修正**:  
   - 若動畫改用其他方式載入（如網路下載），請確保有適當的緩存與釋放機制，避免記憶體洩漏。  
   - 若動畫改用 Compose Lottie API，請確認 Compose 的副作用鍵（key）管理正確，避免重複載入或無限重組。  

3. **嚴重度**: Low  
   **檔名: app/src/main/res/raw/ic_hours.json**  
   **問題說明**: Lottie JSON 檔案刪除不涉及 Coroutine/Flow、RecyclerView、權限、WebView、DeepLink、ANR、Proguard 等風險。  
   **建議修正**: 無需額外修正。  

---

### 回歸測試清單

- 確認 App 中所有使用該 Lottie 動畫的頁面或元件是否正常顯示動畫或替代畫面。  
- 確認刪除該檔案後，App 不會因找不到資源而崩潰。  
- 若有使用 Lottie 動畫播放，測試動畫播放流程是否正常，無記憶體洩漏或異常。  
- 確認 APK 體積是否有明顯減少。  

---

### 需要補充的上下文

- 請提供 Android Gradle Plugin 版本與 targetSdkVersion。  
- 請提供該 Lottie JSON 是否有被 Compose Lottie 或其他動畫框架引用。  
- 請提供該動畫是否有替代方案（如網路載入或其他動畫檔案）。  
- 若有相關 UI 代碼，請提供引用該檔案的 Compose 或 View 代碼。  

---

總結：此 diff 只刪除一個 Lottie JSON 檔案，主要風險在於是否有遺漏清理引用，導致動畫無法顯示或 App 崩潰。建議確認引用狀況並做好回歸測試。

## 區塊 6
本次 diff 主要變動為：
- 刪除兩個 raw 資源檔（ic_next_day.json、loading.json）
- 新增 colors.xml、ids.xml、strings.xml 的少量資源
- config.gradle 版本號與 URL 變更
- release notes 更新
- pxpayplus_sdk.jar 及 debug 版本更新（binary）
- 無程式碼邏輯改動

以下為具體審查建議：

---

1. **Low** | `app/src/main/res/raw/ic_next_day.json` | 刪除動畫資源檔  
   **問題說明**：刪除 Lottie JSON 動畫檔，需確認該動畫是否仍有被 UI 使用，避免造成資源找不到導致崩潰或 UI 異常。  
   **建議修正**：  
   - 確認所有引用該檔案的地方（如 LottieAnimationView、動畫載入程式碼）已移除或替換。  
   - 若仍需使用，請保留或替換為新檔案。  
   - 若確定不再使用，建議同步清理相關引用。  

2. **Low** | `app/src/main/res/raw/loading.json` | 刪除動畫資源檔  
   **問題說明**：同上，刪除 loading 動畫資源，需確認 UI 不再使用該檔案。  
   **建議修正**：  
   - 同上，檢查引用並清理。  

3. **Low** | `app/src/main/res/values/colors.xml` | 新增 color_808080  
   **問題說明**：新增灰色色碼，無明顯問題。  
   **建議修正**：無需修改。  

4. **Low** | `app/src/main/res/values/ids.xml` | 新增 tag_jiggle_animator id  
   **問題說明**：新增 id，需確認該 id 用於 RecyclerView 或動畫等元件時，是否有正確使用且避免重複。  
   **建議修正**：  
   - 確認該 id 用途，避免與其他 id 衝突。  

5. **Low** | `app/src/main/res/values/strings.xml` | 字串修改與新增  
   **問題說明**：字串「愛心捐贈」改為「愛心捐款」，新增多個自訂功能相關字串。  
   **建議修正**：  
   - 確認字串修改是否符合產品需求。  
   - 新增字串建議加上註解，方便後續維護。  

6. **Low** | `config.gradle` | 版本號與 URL 變更  
   **問題說明**：版本號更新，新增 QA 與 Production 短網址。  
   **建議修正**：  
   - 確認 URL 是否安全且符合 HTTPS 標準。  
   - 版本號更新無問題。  

7. **Medium** | `runtime/pxpayplus_sdk.jar` 及 debug 版本 | SDK 更新  
   **問題說明**：更新國泰支付 SDK，binary 檔案差異無法直接審查。  
   **建議修正**：  
   - 確認 SDK 版本更新內容，是否有 API 變更或安全性修正。  
   - 測試支付流程是否正常，避免因 SDK 更新導致支付失敗或崩潰。  
   - 確認 Proguard/R8 設定是否需更新以支援新 SDK。  

---

### 風險點與回歸測試清單

- **動畫資源刪除**：  
  - 測試所有使用該動畫的頁面，確保不會因資源缺失導致崩潰或 UI 異常。  
  - 確認 LottieAnimationView 或其他動畫元件不會嘗試載入已刪除的檔案。  

- **字串修改**：  
  - 確認 UI 顯示文字正確，無誤植或錯誤顯示。  

- **SDK 更新**：  
  - 支付流程完整測試（綁定信用卡、付款、取消等）。  
  - 確認 SDK 新版本無新增權限需求或隱私風險。  
  - 確認 Proguard/R8 混淆規則適用。  

- **config.gradle URL 變更**：  
  - 確認短網址可正常導向，且無安全風險。  

---

### 需要補充的上下文

- Android Gradle Plugin (AGP) 版本  
- targetSdkVersion  
- 是否有使用 LottieAnimationView 或其他動畫元件載入被刪除的 JSON 檔案  
- Proguard/R8 設定檔內容，確認 SDK 更新後是否需要調整  
- SDK 更新的 Release Note 或 Changelog  

---

總結：本次 diff 主要為資源刪除與 SDK 更新，無程式碼邏輯改動，建議重點放在資源引用清理與支付 SDK 更新測試，避免因資源缺失或 SDK 版本不符導致崩潰或支付異常。
