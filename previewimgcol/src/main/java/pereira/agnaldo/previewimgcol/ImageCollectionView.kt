@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package pereira.agnaldo.previewimgcol

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.ablanco.zoomy.Zoomy
import com.bumptech.glide.Glide
import java.io.File
import kotlin.math.roundToInt


class ImageCollectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        const val NO_ROW_LIMITS = -1
    }

    private val previewImages: ArrayList<PreviewImage>

    private var onMoreClickListener: OnMoreClickListener? = null
    private var onMoreClickListenerUnit: ((List<Bitmap>) -> Unit)? = null

    var maxImagePerRow = 3
        set(value) {
            field = value
            clearAndReloadBitmaps()
        }

    var maxRows = NO_ROW_LIMITS
        set(value) {
            field = value
            clearAndReloadBitmaps()
        }

    var baseImageHeight = 300
        set(value) {
            field = value
            clearAndReloadBitmaps()
        }

    var imageMargin = 1
        set(value) {
            field = value
            clearAndReloadBitmaps()
        }

    var mBackgroundColor = Color.WHITE
        set(value) {
            field = value
            clearAndReloadBitmaps()
        }

    var scaleType = ImageView.ScaleType.CENTER_CROP
        set(value) {
            field = value
            clearAndReloadBitmaps()
        }

    var pinchToZoom = true
        set(value) {
            field = value
            clearAndReloadBitmaps()
        }

    var showExternalBorderMargins = false
        set(value) {
            field = value
            clearAndReloadBitmaps()
        }

    init {
        orientation = VERTICAL

        previewImages = ArrayList()
        getStyles(attrs, defStyleAttr)
    }

    private fun getStyles(attrs: AttributeSet?, defStyle: Int) {
        attrs?.let {

            val typedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.ImageCollectionView, defStyle, R.style.defaultPreviewImageCollection
            )

            baseImageHeight = typedArray.getDimensionPixelSize(
                R.styleable.ImageCollectionView_baseRowHeight, baseImageHeight
            )

            imageMargin = typedArray.getDimensionPixelSize(
                R.styleable.ImageCollectionView_imageMargin, imageMargin
            )

            maxImagePerRow = typedArray.getInteger(
                R.styleable.ImageCollectionView_maxImagePerRow, maxImagePerRow
            )

            maxRows = typedArray.getInteger(
                R.styleable.ImageCollectionView_maxRows, maxRows
            )

            val scaleTypeInt = typedArray.getInteger(
                R.styleable.ImageCollectionView_imageScaleType,
                ImageView.ScaleType.CENTER_CROP.ordinal
            )

            ImageView.ScaleType.values().firstOrNull() { it.ordinal == scaleTypeInt }?.let {
                scaleType = it
            }

            mBackgroundColor = typedArray.getColor(
                R.styleable.ImageCollectionView_backgroundColor, mBackgroundColor
            )

            pinchToZoom = typedArray.getBoolean(
                R.styleable.ImageCollectionView_pinchToZoom, pinchToZoom
            )

            showExternalBorderMargins = typedArray.getBoolean(
                R.styleable.ImageCollectionView_showExternalBorderMargins,
                showExternalBorderMargins
            )

            typedArray.recycle()
        }
    }

    fun addImages(bitmaps: ArrayList<Bitmap>) =
        bitmaps.forEach { bmp -> addImage(bmp) }

    fun addImage(drawableRes: Int) =
        addImage(PreviewImage(context, drawableRes), onClick = null, onLongClick = null)

    fun addImage(
        drawableRes: Int,
        onClick: OnImageClickListener? = null,
        onLongClick: OnImageLongClickListener? = null
    ) = addImage(PreviewImage(context, drawableRes), onClick, onLongClick)

    fun addImage(
        drawableRes: Int,
        onClickUnit: ((bitmap: Bitmap?, imageView: ImageView?) -> Unit)? = null,
        onLongClickUnit: ((bitmap: Bitmap?, imageView: ImageView?) -> Unit)? = null
    ) = addImage(PreviewImage(context, drawableRes), onClickUnit, onLongClickUnit)

    fun addImage(drawable: Drawable) =
        addImage(PreviewImage(context, drawable), onClick = null, onLongClick = null)

    fun addImage(
        drawable: Drawable,
        onClick: OnImageClickListener? = null,
        onLongClick: OnImageLongClickListener? = null
    ) = addImage(PreviewImage(context, drawable), onClick, onLongClick)

    fun addImage(
        drawable: Drawable,
        onClickUnit: ((bitmap: Bitmap?, imageView: ImageView?) -> Unit)? = null,
        onLongClickUnit: ((bitmap: Bitmap?, imageView: ImageView?) -> Unit)? = null
    ) = addImage(PreviewImage(context, drawable), onClickUnit, onLongClickUnit)

    fun addImage(bitmap: Bitmap) =
        addImage(PreviewImage(context, bitmap), onClick = null, onLongClick = null)

    fun addImage(
        bitmap: Bitmap,
        onClick: OnImageClickListener? = null,
        onLongClick: OnImageLongClickListener? = null
    ) = addImage(PreviewImage(context, bitmap), onClick, onLongClick)

    fun addImage(
        bitmap: Bitmap,
        onClickUnit: ((bitmap: Bitmap?, imageView: ImageView?) -> Unit)? = null,
        onLongClickUnit: ((bitmap: Bitmap?, imageView: ImageView?) -> Unit)? = null
    ) = addImage(PreviewImage(context, bitmap), onClickUnit, onLongClickUnit)

    fun addImage(bitmapFile: File) =
        addImage(PreviewImage(context, bitmapFile), onClick = null, onLongClick = null)

    fun addImage(
        bitmapFile: File,
        onClick: OnImageClickListener? = null,
        onLongClick: OnImageLongClickListener? = null
    ) = addImage(PreviewImage(context, bitmapFile), onClick, onLongClick)

    fun addImage(
        bitmapFile: File,
        onClickUnit: ((bitmap: Bitmap?, imageView: ImageView?) -> Unit)? = null,
        onLongClickUnit: ((bitmap: Bitmap?, imageView: ImageView?) -> Unit)? = null
    ) = addImage(PreviewImage(context, bitmapFile), onClickUnit, onLongClickUnit)

    fun addImage(bitmapUri: Uri) =
        addImage(PreviewImage(context, bitmapUri), onClick = null, onLongClick = null)

    fun addImage(
        bitmapUri: Uri,
        onClick: OnImageClickListener? = null,
        onLongClick: OnImageLongClickListener? = null
    ) = addImage(PreviewImage(context, bitmapUri), onClick, onLongClick)

    fun addImage(
        bitmapUri: Uri,
        onClickUnit: ((bitmap: Bitmap?, imageView: ImageView?) -> Unit)? = null,
        onLongClickUnit: ((bitmap: Bitmap?, imageView: ImageView?) -> Unit)? = null
    ) = addImage(PreviewImage(context, bitmapUri), onClickUnit, onLongClickUnit)

    private fun addImage(
        previewImage: PreviewImage,
        onClick: OnImageClickListener? = null,
        onLongClick: OnImageLongClickListener? = null
    ) {
        previewImage.mOnClick = onClick
        previewImage.mOnLongClick = onLongClick
        previewImages.add(previewImage)

        afterAddPreviewImage(previewImage)
    }

    private fun addImage(
        previewImage: PreviewImage,
        onClickUnit: ((bitmap: Bitmap?, imageView: ImageView?) -> Unit)? = null,
        onLongClickUnit: ((bitmap: Bitmap?, imageView: ImageView?) -> Unit)? = null
    ) {
        previewImage.mOnClickUnit = onClickUnit
        previewImage.mOnLongClickUnit = onLongClickUnit
        previewImages.add(previewImage)

        afterAddPreviewImage(previewImage)
    }

    private fun afterAddPreviewImage(previewImage: PreviewImage) {
        reEvaluateLastRow(previewImage)
        removeOutsideMargins()
        invalidate()
    }

    fun clearImages() {
        previewImages.clear()
        removeAllViews()
        invalidate()
    }

    fun removeImage(bitmap: Bitmap) {
        findPreviewImage(bitmap)?.let {
            previewImages.remove(it)
        }
        clearAndReloadBitmaps()
        invalidate()
    }

    private fun findPreviewImage(bitmap: Bitmap): PreviewImage? =
        previewImages.firstOrNull { it.isEquals(bitmap) }

    fun setOnMoreClicked(onMoreClickListener: OnMoreClickListener) {
        this.onMoreClickListener = onMoreClickListener
    }

    fun setOnMoreClicked(onMoreClickListenerUnit: (bitmaps: List<Bitmap>) -> Unit) {
        this.onMoreClickListenerUnit = onMoreClickListenerUnit
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post { clearAndReloadBitmaps() }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        previewImages.forEach { previewImage: PreviewImage ->
            previewImage.mImageView?.let {
                Zoomy.unregister(it)
            }
        }
    }

    private fun clearAndReloadBitmaps() {
        removeAllViews()
        extractAndInflateImagesPerLine(previewImages)
        removeOutsideMargins()
    }

    private fun extractAndInflateImagesPerLine(previewImages: List<PreviewImage>) {
        val maxRowReached = maxRows != NO_ROW_LIMITS && childCount == maxRows

        if (!maxRowReached && previewImages.size > maxImagePerRow) {
            val newLine = createNewRow()
            addBitmapsToLine(previewImages.subList(0, maxImagePerRow), newLine)

            extractAndInflateImagesPerLine(
                previewImages.subList(
                    maxImagePerRow,
                    previewImages.size
                )
            )
            return
        } else {
            if (!maxRowReached) {
                val newLine = createNewRow()
                addBitmapsToLine(previewImages, newLine)
            } else {
                addThereAreMore()
            }
        }
    }

    private fun addThereAreMore() {
        val lastRow = getChildAt(childCount - 1) as LinearLayout
        val lastImage = lastRow.getChildAt(lastRow.childCount - 1) as ImageView

        Zoomy.unregister(lastImage)
        val lastImageIndice = childCount * maxImagePerRow

        if (onMoreClickListener != null || onMoreClickListenerUnit != null) {
            lastImage.setOnClickListener {
                val bitmaps = previewImages.subList(
                    lastImageIndice - 1,
                    previewImages.size
                ).mapNotNull { it.asBitmap() }.toList()
                onMoreClickListener?.onMoreClicked(bitmaps)
                onMoreClickListenerUnit?.invoke(bitmaps)
            }
        }

        previewImages[(maxRows * maxImagePerRow) - 1].asBitmap()?.let {
            val blurredBitmap = blur(it)

            val canvas = Canvas(blurredBitmap)

            val paintText = Paint()
            paintText.color = Color.WHITE
            paintText.style = Paint.Style.FILL_AND_STROKE
            paintText.textAlign = Paint.Align.CENTER

            val text = "+".plus(previewImages.size - lastImageIndice + 1)
            val textSize = 130f
            paintText.textSize = 130f

            val paint = Paint()
            paint.color = Color.argb(100, 0, 0, 0)
            paint.maskFilter = BlurMaskFilter(300F, BlurMaskFilter.Blur.INNER)

            val rect = Rect(0, 0, canvas.width, canvas.height)
            canvas.drawRect(rect, paint)

            canvas.drawText(
                text,
                rect.centerX().toFloat(),
                rect.centerY().toFloat() + textSize / 2f,
                paintText
            )

            Glide.with(context).load(blurredBitmap).into(lastImage)
        }
    }

    private fun reEvaluateLastRow(previewImage: PreviewImage) {
        if (width == 0) {
            return
        }

        if (childCount == 0) {
            createNewRow()
        }

        val lineLinearLayout = getChildAt(childCount - 1) as ViewGroup
        val lineChildCount = lineLinearLayout.childCount
        if (lineChildCount == maxImagePerRow) {
            val maxImages = maxRows * maxImagePerRow
            val imagesCount = childCount * maxImagePerRow
            if (imagesCount < maxImages) {
                createNewRow()
                reEvaluateLastRow(previewImage)
            } else {
                addThereAreMore()
            }
            return
        }

        val bitmaps = previewImages.subList(
            previewImages.size - lineChildCount - 1, previewImages.size
        )
        addBitmapsToLine(bitmaps, lineLinearLayout)
    }

    private fun addBitmapsToLine(previewImages: List<PreviewImage>, rowLinearLayout: ViewGroup) {
        if (previewImages.isEmpty())
            return

        rowLinearLayout.removeAllViews()
        rowLinearLayout.setBackgroundColor(mBackgroundColor)

        val widthSum = previewImages.sumBy { previewImage -> previewImage.width() }

        previewImages.forEach { previewImage: PreviewImage ->
            val imageView = ImageView(context)
            imageView.scaleType = scaleType
            previewImage.loadImage(imageView)

            if (pinchToZoom && context is Activity) {
                Zoomy.Builder(context as Activity).target(imageView).tapListener {
                    previewImage.onClick()
                }.longPressListener {
                    previewImage.onLongClick()
                }.register()
            }

            val proportion = (previewImage.width() / widthSum.toFloat())
            val widthBmp = ((width * proportion).toInt()) - (2 * imageMargin)
            val heightBmp = baseImageHeight - (2 * imageMargin)

            val params = LayoutParams(widthBmp, heightBmp)
            params.setMargins(imageMargin, imageMargin, imageMargin, imageMargin)

            rowLinearLayout.addView(imageView, params)
        }
    }

    private fun createNewRow(): LinearLayout {
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = HORIZONTAL

        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(linearLayout, params)
        return linearLayout
    }

    private fun removeOutsideMargins() {
        if (showExternalBorderMargins)
            return

        for (i in 0 until childCount) {
            val row = getChildAt(i) as LinearLayout
            val rowChildCount = row.childCount
            for (j in 0 until rowChildCount) {
                val image = row.getChildAt(j)

                val layoutParams = image.layoutParams as LayoutParams
                if (i == 0) {
                    layoutParams.topMargin = 0
                    layoutParams.height = layoutParams.height + imageMargin
                }

                if (i == childCount - 1) {
                    layoutParams.bottomMargin = 0
                    layoutParams.height = layoutParams.height + imageMargin
                }

                if (j == 0) {
                    layoutParams.leftMargin = 0
                    layoutParams.width = layoutParams.width + imageMargin
                }

                if (j == rowChildCount - 1) {
                    layoutParams.rightMargin = 0
                    layoutParams.width = layoutParams.width + imageMargin
                }

                image.layoutParams = layoutParams
            }
        }
    }

    private fun blur(image: Bitmap): Bitmap {

        val bitmapScale = 0.4f
        val blurRadius = 15.5f

        val width = (image.width * bitmapScale).roundToInt()
        val height = (image.height * bitmapScale).roundToInt()

        val inputBitmap = Bitmap.createScaledBitmap(image, width, height, false)
        val outputBitmap = Bitmap.createBitmap(inputBitmap)

        val rs = RenderScript.create(context)
        val theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
        theIntrinsic.setRadius(blurRadius)
        theIntrinsic.setInput(tmpIn)
        theIntrinsic.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)

        return outputBitmap
    }

    fun getImageAt(index: Int): Bitmap {
        return previewImages[index].asBitmap()!!
    }

    fun getNullableImageAt(index: Int): Bitmap? {
        return if (index < previewImages.size) previewImages[index].asBitmap() else null
    }

    interface OnImageClickListener {
        fun onClick(bitmap: Bitmap, imageView: ImageView)
    }

    interface OnImageLongClickListener {
        fun onLongClick(bitmap: Bitmap, imageView: ImageView)
    }

    interface OnMoreClickListener {
        fun onMoreClicked(bitmaps: List<Bitmap>)
    }

}
