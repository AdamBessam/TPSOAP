package ma.ensa.soaptp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import ma.ensa.soaptp.R
import ma.ensa.soaptp.beans.Compte
import java.text.SimpleDateFormat
import java.util.Locale

class AccountAdapter : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {
    private var accounts = mutableListOf<Compte>()
    var onAccountEdit: ((Compte) -> Unit)? = null
    var onAccountDelete: ((Compte) -> Unit)? = null

    fun updateAccountList(newAccounts: List<Compte>) {
        accounts.clear()
        accounts.addAll(newAccounts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(accounts[position])
    }

    override fun getItemCount() = accounts.size

    fun removeAccount(account: Compte) {
        val position = accounts.indexOf(account)
        if (position >= 0) {
            accounts.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    inner class AccountViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val accountIdTextView: TextView = view.findViewById(R.id.tvId)
        private val accountBalanceTextView: TextView = view.findViewById(R.id.tvSolde)
        private val accountTypeChip: Chip = view.findViewById(R.id.tvType)
        private val accountDateTextView: TextView = view.findViewById(R.id.tvDate)
        private val editButton: MaterialButton = view.findViewById(R.id.btnEdit)
        private val deleteButton: MaterialButton = view.findViewById(R.id.btnDelete)

        fun bind(account: Compte) {
            accountIdTextView.text = "Compte N° ${account.id}"
            accountBalanceTextView.text = "${account.solde} DH"
            accountTypeChip.text = account.type.name
            accountDateTextView.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(account.dateCreation)

            editButton.setOnClickListener {
                onAccountEdit?.invoke(account)
            }

            deleteButton.setOnClickListener {
                onAccountDelete?.invoke(account)
            }
        }
    }
}