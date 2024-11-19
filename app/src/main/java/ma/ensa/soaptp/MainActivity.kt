package ma.ensa.soaptp

import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensa.soaptp.adapter.CompteAdapter
import ma.ensa.soaptp.beans.TypeCompte
import ma.ensa.soaptp.webservice.SoapService

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private val adapter = CompteAdapter()
    private val soapService = SoapService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        setupListeners()
        loadComptes()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.fabAdd)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        adapter.onDeleteClick = { compte ->
            MaterialAlertDialogBuilder(this)
                .setTitle("Supprimer le compte")
                .setMessage("Voulez-vous vraiment supprimer ce compte ?")
                .setPositiveButton("Supprimer") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        val success = soapService.deleteCompte(compte.id!!)
                        withContext(Dispatchers.Main) {
                            if (success) {
                                // Remove the deleted compte directly from the adapter
                                adapter.removeCompte(compte)
                            } else {
                                adapter.removeCompte(compte)
                            }
                        }
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
        }

    }

    private fun setupListeners() {
        fabAdd.setOnClickListener { showAddCompteDialog() }

    }

    private fun showAddCompteDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_account, null)

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setTitle("Nouveau compte")
            .setPositiveButton("Ajouter") { _, _ ->
                val etSolde = dialogView.findViewById<TextInputEditText>(R.id.etSolde)
                val radioCourant = dialogView.findViewById<RadioButton>(R.id.radioCourant)

                val solde = etSolde.text.toString().toDoubleOrNull() ?: 0.0
                val type = if (radioCourant.isChecked) TypeCompte.COURANT else TypeCompte.EPARGNE

                lifecycleScope.launch(Dispatchers.IO) {
                    soapService.createCompte(solde, type)
                    loadComptes()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun loadComptes() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val comptes = soapService.getComptes()
                withContext(Dispatchers.Main) {
                    if (comptes.isNotEmpty()) {
                        adapter.updateComptes(comptes)
                    } else {
                        Toast.makeText(this@MainActivity, "Aucun compte trouvé", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}