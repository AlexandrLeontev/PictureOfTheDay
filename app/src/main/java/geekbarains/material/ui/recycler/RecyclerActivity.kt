package geekbarains.material.ui.recycler

import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import geekbarains.material.R
import kotlinx.android.synthetic.main.activity_recycler.*
import kotlinx.android.synthetic.main.activity_recycler_item_earth.view.*
import kotlinx.android.synthetic.main.activity_recycler_item_mars.view.*
import kotlin.math.abs

class RecyclerActivity : AppCompatActivity() {

    private var isNewList = false
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var adapter: RecyclerActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)
        val data = arrayListOf(
                Pair(Data(1, "Mars", ""), false)
        )

        data.add(0, Pair(Data(0, "Header"), false))

        adapter = RecyclerActivityAdapter(
                object : RecyclerActivityAdapter.OnListItemClickListener {
                    override fun onItemClick(data: Data) {
                        Toast.makeText(this@RecyclerActivity, data.someText, Toast.LENGTH_SHORT).show()
                    }
                },
                data,
                object : RecyclerActivityAdapter.OnStartDragListener {
                    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
                        itemTouchHelper.startDrag(viewHolder)
                    }
                }
        )

        recyclerView.adapter = adapter
        recyclerActivityFAB.setOnClickListener { adapter.appendItem() }
        itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(adapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)
        recyclerActivityDiffUtilFAB.setOnClickListener { changeAdapterData() }
    }

    private fun changeAdapterData() {
        adapter.setItems(createItemList(isNewList).map { it })
        isNewList = !isNewList
    }

    private fun createItemList(instanceNumber: Boolean): List<Pair<Data, Boolean>> {
        return when (instanceNumber) {
            false -> listOf(
                    Pair(Data(0, "Header"), false),
                    Pair(Data(1, "Mars", ""), false),
                    Pair(Data(2, "Mars", ""), false),
                    Pair(Data(3, "Mars", ""), false),
                    Pair(Data(4, "Mars", ""), false),
                    Pair(Data(5, "Mars", ""), false),
                    Pair(Data(6, "Mars", ""), false)
            )
            true -> listOf(
                    Pair(Data(0, "Header"), false),
                    Pair(Data(1, "Mars", ""), false),
                    Pair(Data(2, "Jupiter", ""), false),
                    Pair(Data(3, "Mars", ""), false),
                    Pair(Data(4, "Neptune", ""), false),
                    Pair(Data(5, "Saturn", ""), false),
                    Pair(Data(6, "Mars", ""), false)
            )
        }
    }
}

class RecyclerActivityAdapter(
        private val onListItemClickListener: OnListItemClickListener,
        private var data: MutableList<Pair<Data, Boolean>>,
        private val dragListener: OnStartDragListener
) :
        RecyclerView.Adapter<BaseViewHolder>(), ItemTouchHelperAdapter {
    // у  RecyclerView есть 3 основные метода, которые нужно реализовать: onCreateViewHolder,onBindViewHolder,getItemCount(),
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        // в onCreateViewHolder мы создаём ViewHolder и его инфлейтим
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_EARTH -> EarthViewHolder(
                    inflater.inflate(R.layout.activity_recycler_item_earth, parent, false) as View
            )
            TYPE_MARS ->
                MarsViewHolder(
                        inflater.inflate(R.layout.activity_recycler_item_mars, parent, false) as View
                )
            else -> HeaderViewHolder(
                    inflater.inflate(R.layout.activity_recycler_item_header, parent, false) as View
            )
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        //onBindViewHolder нужен для того, чтобы внутрь ViewHolder положить какие-то данные и он их затем отображал
        holder.bind(data[position])
    }

    override fun onBindViewHolder(
            holder: BaseViewHolder,
            position: Int,
            payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads)
        else {
            val combinedChange =
                    createCombinedPayload(payloads as List<Change<Pair<Data, Boolean>>>)
            val oldData = combinedChange.oldData
            val newData = combinedChange.newData

            if (newData.first.someText != oldData.first.someText) {
                holder.itemView.marsTextView.text = newData.first.someText
            }
        }
    }

    override fun getItemCount(): Int {
        // getItemCount() возвращает колличество элементов, которые находятся внутри адаптера
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> TYPE_HEADER
            data[position].first.someDescription.isNullOrBlank() -> TYPE_MARS
            else -> TYPE_EARTH
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        data.removeAt(fromPosition).apply {
            data.add(if (toPosition > fromPosition) toPosition - 1 else toPosition, this)
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemDismiss(position: Int) {
        data.removeAt(position)
        notifyItemRemoved(position)
    }

    fun setItems(newItems: List<Pair<Data, Boolean>>) { //было
        // fun setItems(newItems: MutableList<Pair<Data, Boolean>>) { // стало
        val calback = DiffUtilCallback(data, newItems)
        val result = DiffUtil.calculateDiff(calback)  // стало
        // val result = DiffUtil.calculateDiff(DiffUtilCallback(data, newItems)) // было
        data.clear() // чистим старые данные // после изменения List на MutableList в data.clear() и data.addAll(newItems) не нужны
        data.addAll(newItems) //добавляем новые
        result.dispatchUpdatesTo(this)
    }

    fun appendItem() {
        data.add(generateItem())
        notifyItemInserted(itemCount - 1)
    }

    private fun generateItem() = Pair(Data(1, "Mars", ""), false)

    inner class DiffUtilCallback(
            private var oldItems: List<Pair<Data, Boolean>>, // старые данные
            private var newItems: List<Pair<Data, Boolean>> // новые данные
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = // следит за id viewHolder-ов
                // здесь сравниваем по id
                oldItems[oldItemPosition].first.id == newItems[newItemPosition].first.id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = // отвечает за наполнение ViewHolder.
        // В нашем случае за текст описания планеты Марс
                //здесь сравниваем по контенту
                oldItems[oldItemPosition].first.someText == newItems[newItemPosition].first.someText

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            // узнаём как наши данные изменились
            val oldItem = oldItems[oldItemPosition]
            val newItem = newItems[newItemPosition]

            return Change(
                    oldItem,
                    newItem
            )
        }
    }

    inner class EarthViewHolder(view: View) : BaseViewHolder(view) {

        override fun bind(dataItem: Pair<Data, Boolean>) {
            if (layoutPosition != RecyclerView.NO_POSITION) {
                itemView.descriptionTextView.text = dataItem.first.someDescription
                itemView.wikiImageView.setOnClickListener {
                    onListItemClickListener.onItemClick(
                            dataItem.first
                    )
                }
            }
        }
    }

    inner class MarsViewHolder(view: View) : BaseViewHolder(view), ItemTouchHelperViewHolder {

        override fun bind(dataItem: Pair<Data, Boolean>) {
            itemView.marsImageView.setOnClickListener { onListItemClickListener.onItemClick(dataItem.first) }
            itemView.addItemImageView.setOnClickListener { addItem() }
            itemView.removeItemImageView.setOnClickListener { removeItem() }
            itemView.moveItemDown.setOnClickListener { moveDown() }
            itemView.moveItemUp.setOnClickListener { moveUp() }
            // moveDown() и moveUp() отвечают за движения вверх и вниз красными стрелочками
            itemView.marsDescriptionTextView.visibility =
                    if (dataItem.second) View.VISIBLE else View.GONE
            itemView.marsTextView.setOnClickListener { toggleText() }
            itemView.dragHandleImageView.setOnTouchListener { _, event ->
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    dragListener.onStartDrag(this)
                }
                false
            }
        }

        private fun addItem() { // добавляет планету нажатием на +
            data.add(layoutPosition, generateItem())
            notifyItemInserted(layoutPosition) // показываем что произошли изменения и recyclerView готовит анимацию
        }

        private fun removeItem() {
            data.removeAt(layoutPosition)
            notifyItemRemoved(layoutPosition)
        }

        private fun moveUp() {
            layoutPosition.takeIf { it > 1 }?.also { currentPosition ->
                data.removeAt(currentPosition).apply {
                    data.add(currentPosition - 1, this)
                }
                //data.removeAt передвигаем элементы внутри коллекции
                // notifyDataSetChanged() // переход без анимации
                notifyItemMoved(currentPosition, currentPosition - 1) // переход с анимацией
            }
        }

        private fun moveDown() {
            layoutPosition.takeIf { it < data.size - 1 }?.also { currentPosition ->
                data.removeAt(currentPosition).apply {
                    data.add(currentPosition + 1, this)
                }
                notifyItemMoved(currentPosition, currentPosition + 1)
            }
        }

        private fun toggleText() {
            data[layoutPosition] = data[layoutPosition].let {
                it.first to !it.second
            }
            notifyItemChanged(layoutPosition) // при нажатии на планету происходит моргание
        }

        override fun onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            itemView.setBackgroundColor(Color.WHITE)
        }
    }

    inner class HeaderViewHolder(view: View) : BaseViewHolder(view) {

        override fun bind(dataItem: Pair<Data, Boolean>) {
            itemView.setOnClickListener {
                onListItemClickListener.onItemClick(dataItem.first)
//                data[1] = Pair(Data("Jupiter", ""), false)
//                notifyItemChanged(1, Pair(Data("", ""), false))
            }
        }
    }

    interface OnListItemClickListener {
        fun onItemClick(data: Data)
    }

    interface OnStartDragListener {
        fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
    }

    companion object {
        private const val TYPE_EARTH = 0
        private const val TYPE_MARS = 1
        private const val TYPE_HEADER = 2
    }
}

data class Data(
        val id: Int = 0,
        val someText: String = "Text",
        val someDescription: String? = "Description"
)

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int)
    fun onItemDismiss(position: Int)
}

interface ItemTouchHelperViewHolder {
    fun onItemSelected()
    fun onItemClear()
}

class ItemTouchHelperCallback( // понимает какое действие произошло. СВАЙП ИЛИ ИЗМЕНЕНИЕ ПОЗИЦИИ
        // позволяет таскать элемент при зажатии на нем курсора
        private val adapter: RecyclerActivityAdapter
        ) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean { // позволяем работу со свайпами
        return true // да
    }

    override fun getMovementFlags( // определяем в какую сторону разрешено двигать элементы
            recyclerView: RecyclerView, // определяем нужный вьюХолдер в котором будет разрешено действие
            viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN // верх и низ
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END // лево и право. End вместо Right нужен для учёта Арабских стран
        return makeMovementFlags(
                dragFlags,
                swipeFlags
        )
    }

    override fun onMove( // замечает, что произошло изменение двух ViewHolder-ов и хорошо бы изменить данные в адаптере
            recyclerView: RecyclerView,
            source: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onItemMove(source.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) { // произошёл свайп и после идёт изменение элемента
        adapter.onItemDismiss(viewHolder.adapterPosition)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            val itemViewHolder = viewHolder as ItemTouchHelperViewHolder
            itemViewHolder.onItemSelected()
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        val itemViewHolder = viewHolder as ItemTouchHelperViewHolder
        itemViewHolder.onItemClear()
    }

    override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) { // отвечает за изменение прозачности вьюшки после свайпа в крайнее положение
            val width = viewHolder.itemView.width.toFloat()
            val alpha = 1.0f - abs(dX) / width
            viewHolder.itemView.alpha = alpha
            viewHolder.itemView.translationX = dX
        } else {
            super.onChildDraw(
                    c, recyclerView, viewHolder, dX, dY,
                    actionState, isCurrentlyActive
            )
        }
    }
}
