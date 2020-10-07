import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 A bank account has a balance that can be changed by
 deposits and withdrawals.
 */
public class BankAccount
{
    private double balance;
    private Lock balanceChange;
    private Condition sufficientFunds;
    private Condition overloadFunds;
    private int maxBalance;
    /**
     Constructs a bank account with a zero balance.
     */
    public BankAccount()
    {
        balance = 0;
        balanceChange = new ReentrantLock();
        sufficientFunds = balanceChange.newCondition();
        overloadFunds = balanceChange.newCondition();
        maxBalance = 100000;
    }

    /**
     Constructs a bank account with a given balance.
     @param initialBalance the initial balance
     */
    public BankAccount(double initialBalance)
    {
        balance = initialBalance;
    }

    /**
     Deposits money into the bank account.
     @param amount the amount to deposit
     */
    public void deposit(double amount) throws InterruptedException { balanceChange.lock();
        try
        {
            while(balance + amount >= maxBalance)
            {
                System.out.println("Depositing halted, too much money " + balance);
                overloadFunds.await();
            }
            System.out.print("Depositing " + amount);
            double newBalance = balance + amount;
            System.out.println(", new balance is " + newBalance);
            balance = newBalance;
            sufficientFunds.signalAll();
        }
        finally
        {
            balanceChange.unlock();
        }
    }

    /**
     Withdraws money from the bank account.
     @param amount the amount to withdraw
     */
    public void withdraw(double amount) throws InterruptedException
    {
        balanceChange.lock();
        try
        {
            while(balance < amount) { sufficientFunds.await(); }
            System.out.println("Withdrawing " + amount);
            double newBalance = balance - amount;
            System.out.println(" , your new balance is " + newBalance);
            balance = newBalance;
            if (newBalance <= maxBalance)
            {
                System.out.println("Allowed Depositing" + balance);
                overloadFunds.signalAll();
            }
        }
        finally
        {
            balanceChange.unlock();
        }
    }

    /**
     Gets the current balance of the bank account.
     @return the current balance
     */
    public double getBalance()
    {
        return balance;
    }
}