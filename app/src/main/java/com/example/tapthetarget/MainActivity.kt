package com.example.tapthetarget

import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Random

class MainActivity : AppCompatActivity() {

    // ==== Data Permainan ====
    private var skor = 0
    private val durasiPermainan = 30_000L
    private var sisaWaktuDetik = 30
    private var timer: CountDownTimer? = null
    private var sedangDijeda = false
    private val acak = Random()

    // ==== View Components ====
    private lateinit var frameGame: FrameLayout
    private lateinit var imgTarget: ImageView
    private lateinit var tvSkor: TextView
    private lateinit var tvWaktu: TextView
    private lateinit var btnPause: Button
    private lateinit var overlayPause: LinearLayout
    private lateinit var overlayGameOver: LinearLayout
    private lateinit var tvSkorJeda: TextView
    private lateinit var tvSkorAkhir: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Inisialisasi View
        frameGame = findViewById(R.id.frameGame)
        imgTarget = findViewById(R.id.imgTarget)
        tvSkor = findViewById(R.id.tvSkor)
        tvWaktu = findViewById(R.id.tvWaktu)
        btnPause = findViewById(R.id.btnPause)
        overlayPause = findViewById(R.id.overlayPause)
        overlayGameOver = findViewById(R.id.overlayGameOver)
        tvSkorJeda = findViewById(R.id.tvSkorJeda)
        tvSkorAkhir = findViewById(R.id.tvSkorAkhir)

        // 2. Pasang Event Listener
        imgTarget.setOnClickListener { targetDitekan() }
        btnPause.setOnClickListener { jedaPermainan() }
        findViewById<Button>(R.id.btnLanjutkan).setOnClickListener { lanjutkanPermainan() }
        findViewById<Button>(R.id.btnUlangi).setOnClickListener { mulaiPermainan() }
        findViewById<Button>(R.id.btnMainLagi).setOnClickListener { mulaiPermainan() }

        // 3. Jalankan Permainan
        mulaiPermainan()
    }

    private fun mulaiPermainan() {
        skor = 0
        sisaWaktuDetik = 30
        sedangDijeda = false

        tvSkor.text = getString(R.string.label_skor, skor)
        tvWaktu.text = getString(R.string.label_waktu, sisaWaktuDetik)

        overlayPause.visibility = View.GONE
        overlayGameOver.visibility = View.GONE
        overlayGameOver.alpha = 1f
        imgTarget.visibility = View.VISIBLE
        imgTarget.isEnabled = true
        btnPause.isEnabled = true

        frameGame.post { pindahkanTarget() }
        jalankanTimer(durasiPermainan)
    }

    private fun pindahkanTarget() {
        val lebarArea = frameGame.width
        val tinggiArea = frameGame.height

        if (lebarArea == 0 || tinggiArea == 0) return

        val jarakAman = 24
        val maxX = lebarArea - imgTarget.width - (jarakAman * 2)
        val maxY = tinggiArea - imgTarget.height - (jarakAman * 2)

        if (maxX <= 0 || maxY <= 0) return

        val posisiX = jarakAman + acak.nextInt(maxX)
        val posisiY = jarakAman + acak.nextInt(maxY)

        imgTarget.x = posisiX.toFloat()
        imgTarget.y = posisiY.toFloat()
    }

    private fun targetDitekan() {
        if (sedangDijeda) return

        skor += 10
        tvSkor.text = getString(R.string.label_skor, skor)

        // Animasi tekan target
        imgTarget.animate()
            .scaleX(0.6f).scaleY(0.6f).setDuration(70)
            .withEndAction {
                imgTarget.animate().scaleX(1f).scaleY(1f).setDuration(70).start()
            }
            .start()

        tampilkanPoinMelayang()
        pindahkanTarget()
    }

    private fun tampilkanPoinMelayang() {
        val label = TextView(this).apply {
            text = "+10"
            setTextColor(0xFF86EFAC.toInt())
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
        }

        frameGame.addView(label)
        label.x = imgTarget.x + 20f
        label.y = imgTarget.y - 30f

        label.animate()
            .alpha(0f)
            .translationYBy(-60f)
            .setDuration(600)
            .withEndAction { frameGame.removeView(label) }
            .start()
    }

    private fun jalankanTimer(durasi: Long) {
        timer?.cancel()
        timer = object : CountDownTimer(durasi, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                sisaWaktuDetik = (millisUntilFinished / 1000).toInt()
                tvWaktu.text = getString(R.string.label_waktu, sisaWaktuDetik)
            }

            override fun onFinish() {
                sisaWaktuDetik = 0
                tvWaktu.text = getString(R.string.label_waktu, 0)
                gameOver()
            }
        }.start()
    }

    private fun jedaPermainan() {
        if (sedangDijeda || overlayGameOver.visibility == View.VISIBLE) return

        sedangDijeda = true
        timer?.cancel()
        imgTarget.isEnabled = false

        tvSkorJeda.text = skor.toString()
        overlayPause.alpha = 0f
        overlayPause.visibility = View.VISIBLE
        overlayPause.animate().alpha(1f).setDuration(200).start()
    }

    private fun lanjutkanPermainan() {
        if (!sedangDijeda) return

        sedangDijeda = false
        overlayPause.visibility = View.GONE
        imgTarget.isEnabled = true

        jalankanTimer(sisaWaktuDetik * 1000L)
    }

    private fun gameOver() {
        timer?.cancel()
        sedangDijeda = false

        imgTarget.isEnabled = false
        btnPause.isEnabled = false
        overlayPause.visibility = View.GONE

        tvSkorAkhir.text = skor.toString()
        overlayGameOver.alpha = 0f
        overlayGameOver.visibility = View.VISIBLE
        overlayGameOver.animate().alpha(1f).setDuration(350).start()

        imgTarget.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}