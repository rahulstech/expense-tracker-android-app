package dreammaker.android.expensetracker.database;

interface ExpensesContract
{
    interface Tables{
        String ACCOUNTS_TABLE = "accounts";
        String PERSONS_TABLE = "persons";
        String TRANSACTIONS_TABLE = "transactions";
    }
	
	interface Views{
        String ABOUT_ACCOUNTS_VIEW = "about_accounts";
        String ABOUT_PERSONS_VIEW = "about_persons";
		String TRANSACTION_DETAILS_VIEW = "transaction_details";
	}

	interface Indexes{
        String TRANSACTIONS_ACCOUNT_ID_INDEX = "transactions_account_id_index";
        String TRANSACTIONS_PERSON_ID_INDEX = "transactions_person_id_index";
    }
    
    interface AccountsColumns {
        String _ID = "_id";
        String ACCOUNT_NAME = "account_name";
    }
    
    interface AboutAccountColumns extends AccountsColumns {
        String BALANCE = "balance";
    }
    
    interface PersonsColumns {
        String _ID = "_id";
        String PERSON_NAME = "person_name";
    }
    
    interface AboutPersonColumns extends PersonsColumns {
        String DUE_PAYMENT = "due";
        String ADVANCED_PAYMENT = "advanced";
    }
    
    interface TransactionsColumns {
        String _ID = "_id";
        String ACCOUNT_ID = "account_id";
        String PERSON_ID = "person_id";
        String AMOUNT = "amount";
        String TYPE = "type";
        String DATE = "date";
        String DESCRIPTION = "description";
        
        int TYPE_CREDIT = 0;
        int TYPE_DEBIT = 1;
    }

    interface BalanceAndDueSummaryColumns {
        String TOTAL_BALANCE = "total_balance";
        String COUNT_ACCOUNTS = "count_accounts";
        String TOTAL_DUE = "total_due";
        String COUNT_PEOPLE = "count_people";
    }
}
