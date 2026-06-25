package com.granjasilabas.app

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Color
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexboxLayout
import com.granjasilabas.app.databinding.ActivityGameBinding
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private val animales = AnimalData.lista.shuffled()
    private var currentIndex = 0
    private val slotContents = mutableMapOf<Int, String?>()
    private var draggedSyllable: String? = null
    private var draggedFromSlot: Int? = null

    private lateinit var soundPool: SoundPool
    private var soundWin: Int = 0
    private var soundPlace: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSound()
        loadAnimal(currentIndex)

        binding.btnSiguiente.setOnClickListener {
            binding.layoutCelebrar.visibility = View.GONE
            currentIndex++
            if (currentIndex >= animales.size) {
                showFinalCelebration()
            } else {
                loadAnimal(currentIndex)
            }
        }

        binding.btnReiniciar.setOnClickListener {
            binding.layoutFinal.visibility = View.GONE
            currentIndex = 0
            loadAnimal(currentIndex)
        }

        binding.btnSalir.setOnClickListener {
            finish()
        }
    }

    private fun setupSound() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attrs).build()
        soundWin   = soundPool.load(this, R.raw.win,   1)
        soundPlace = soundPool.load(this, R.raw.place, 1)
    }

    private fun loadAnimal(idx: Int) {
        val animal = animales[idx]
        slotContents.clear()
        animal.silabas.forEachIndexed { i, _ -> slotContents[i] = null }

        binding.tvAnimalEmoji.text = animal.emoji
        // FIX 2: mostrar progreso
        binding.tvProgreso.text = "${idx + 1} de ${animales.size}"
        binding.layoutCelebrar.visibility = View.GONE
        binding.layoutFinal.visibility = View.GONE

        buildPool(animal)
        buildSlots(animal)
    }

    // ── POOL ──────────────────────────────────────────────────────────

    private fun buildPool(animal: Animal) {
        binding.poolSilabas.removeAllViews()
        animal.silabas.shuffled().forEach { syl ->
            binding.poolSilabas.addView(makeChip(syl, fromSlot = null))
        }
    }

    private fun makeChip(syl: String, fromSlot: Int?): TextView {
        val chip = TextView(this).apply {
            text = syl
            textSize = 24f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            // FIX 3: más padding para que no se corte el texto
            setPadding(32, 28, 32, 28)
            background = ContextCompat.getDrawable(context, R.drawable.bg_chip)
            tag = syl

            val lp = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(12, 12, 12, 12)
            layoutParams = lp
        }

        chip.setOnLongClickListener { v ->
            draggedSyllable = syl
            draggedFromSlot = fromSlot
            val item     = ClipData.Item(syl)
            val dragData = ClipData(syl, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
            v.startDragAndDrop(dragData, View.DragShadowBuilder(v), v, 0)
            v.visibility = View.INVISIBLE
            true
        }

        chip.setOnTouchListener(TouchDragHelper { syl2, slotFrom ->
            draggedSyllable = syl2
            draggedFromSlot = slotFrom
        })

        return chip
    }

    // ── SLOTS ─────────────────────────────────────────────────────────

    private fun buildSlots(animal: Animal) {
        binding.containerSlots.removeAllViews()
        animal.silabas.indices.forEach { i ->
            binding.containerSlots.addView(makeSlot(i))
        }
    }

    private fun makeSlot(idx: Int): FrameLayout {
        val slot = FrameLayout(this).apply {
            // FIX 3: slots más anchos para que entre el texto completo
            val lp = android.widget.LinearLayout.LayoutParams(220, 150)
            lp.setMargins(12, 0, 12, 0)
            layoutParams = lp
            background = ContextCompat.getDrawable(context, R.drawable.bg_slot)
            tag = idx
        }

        slot.setOnDragListener { view, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_ENTERED -> {
                    view.background = ContextCompat.getDrawable(this, R.drawable.bg_slot_over)
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    view.background = ContextCompat.getDrawable(this, R.drawable.bg_slot)
                    true
                }
                DragEvent.ACTION_DROP -> {
                    view.background = ContextCompat.getDrawable(this, R.drawable.bg_slot)
                    val syl = draggedSyllable ?: return@setOnDragListener false
                    handleDrop(syl, idx)
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    val dragged = event.localState as? View
                    if (!event.result) dragged?.visibility = View.VISIBLE
                    true
                }
                else -> true
            }
        }
        return slot
    }

    private fun handleDrop(syl: String, toSlot: Int) {
        val fromSlot = draggedFromSlot
        val animal   = animales[currentIndex]

        // FIX 1: verificar si la sílaba es correcta para ESTE slot
        val isCorrect = animal.silabas[toSlot] == syl

        if (!isCorrect) {
            // Rebotar: devolver al lugar de origen sin hacer nada
            if (fromSlot != null) {
                // Venía de un slot: restaurar visibilidad del chip en ese slot
                refreshSlotView(fromSlot)
            } else {
                // Venía del pool: hacer visible el chip del pool de nuevo
                val pool = binding.poolSilabas
                for (i in 0 until pool.childCount) {
                    val v = pool.getChildAt(i)
                    if (v.tag == syl && v.visibility == View.INVISIBLE) {
                        // Animación de rebote antes de volver
                        v.visibility = View.VISIBLE
                        animateRebote(v)
                        break
                    }
                }
            }
            draggedSyllable = null
            draggedFromSlot = null
            return
        }

        // Es correcta: proceder normalmente
        if (fromSlot != null) {
            slotContents[fromSlot] = null
            refreshSlotView(fromSlot)
        } else {
            removeFromPool(syl)
        }

        val displaced = slotContents[toSlot]
        if (displaced != null) addToPool(displaced)

        slotContents[toSlot] = syl
        refreshSlotView(toSlot)
        soundPool.play(soundPlace, 0.6f, 0.6f, 1, 0, 1f)
        checkWin()
    }

    private fun animateRebote(v: View) {
        val tx = ObjectAnimator.ofFloat(v, "translationX", 0f, -18f, 18f, -12f, 12f, 0f)
        tx.duration = 350
        tx.start()
    }

    private fun refreshSlotView(idx: Int) {
        val slot = binding.containerSlots.findViewWithTag<FrameLayout>(idx) ?: return
        slot.removeAllViews()
        val syl = slotContents[idx] ?: return
        val chip = makeChip(syl, fromSlot = idx)
        chip.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        slot.addView(chip)
        chip.visibility = View.VISIBLE
    }

    private fun removeFromPool(syl: String) {
        val pool = binding.poolSilabas
        for (i in 0 until pool.childCount) {
            val v = pool.getChildAt(i)
            if (v.tag == syl && v.visibility == View.INVISIBLE) {
                pool.removeView(v)
                return
            }
        }
    }

    private fun addToPool(syl: String) {
        binding.poolSilabas.addView(makeChip(syl, fromSlot = null))
    }

    // ── VICTORIA ──────────────────────────────────────────────────────

    private fun checkWin() {
        val animal  = animales[currentIndex]
        val correct = animal.silabas.indices.all { slotContents[it] == animal.silabas[it] }
        if (correct) {
            Handler(Looper.getMainLooper()).postDelayed({
                soundPool.play(soundWin, 1f, 1f, 1, 0, 1f)
                launchConfetti()
                showCelebration()
            }, 200)
        }
    }

    private fun showCelebration() {
        binding.layoutCelebrar.visibility = View.VISIBLE
        val scaleX = ObjectAnimator.ofFloat(binding.tvCapybara, "scaleX", 0f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.tvCapybara, "scaleY", 0f, 1.2f, 1f)
        AnimatorSet().apply { playTogether(scaleX, scaleY); duration = 400; start() }
    }

    // FIX 4: pantalla final al terminar la serie
    private fun showFinalCelebration() {
        soundPool.play(soundWin, 1f, 1f, 1, 0, 1f)
        launchConfetti()
        binding.layoutFinal.visibility = View.VISIBLE
        val scaleX = ObjectAnimator.ofFloat(binding.tvCapybaraFinal, "scaleX", 0f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.tvCapybaraFinal, "scaleY", 0f, 1.2f, 1f)
        AnimatorSet().apply { playTogether(scaleX, scaleY); duration = 400; start() }
    }

    private fun launchConfetti() {
        binding.konfettiView.start(
            Party(
                speed = 10f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(
                    0xFFE07B39.toInt(), 0xFF4A9E3F.toInt(),
                    0xFFF5C842.toInt(), 0xFF3A8FD4.toInt(),
                    0xFFE84393.toInt()
                ),
                emitter = Emitter(duration = 3, TimeUnit.SECONDS).perSecond(80),
                position = Position.Relative(0.5, 0.0)
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}
