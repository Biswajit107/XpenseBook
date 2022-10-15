package com.app.biswajit.xpensebook;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.app.biswajit.xpensebook.entity.Expense;

import java.text.NumberFormat;
import java.util.List;

import androidx.core.content.ContextCompat;
import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.LongPressAwareTableDataAdapter;

public class ExpenseTableDataAdapter extends LongPressAwareTableDataAdapter<Expense> {
    private static final int TEXT_SIZE = 14;
    private static final NumberFormat PRICE_FORMATTER = NumberFormat.getNumberInstance();

    public ExpenseTableDataAdapter(final Context context, final List<Expense> data, final TableView<Expense> tableView) {
        super(context, data, tableView);
    }

    @Override
    public View getDefaultCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        final Expense expense = getRowData(rowIndex);
        View renderedView = null;

        switch (columnIndex) {
            case 0:
                renderedView = renderSource(expense);
                break;
            case 1:
                renderedView = renderDestination(expense);
                break;
            case 2:
                renderedView = renderPrice(expense);
                break;
         }

        return renderedView;
    }

    @Override
    public View getLongPressCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        final Expense car = getRowData(rowIndex);
        View renderedView = null;

        switch (columnIndex) {
            case 1:
                //renderedView = renderDate(car);
                break;
            default:
                renderedView = getDefaultCellView(rowIndex, columnIndex, parentView);
        }

        return renderedView;
    }

//    private View renderEditableCatName(final Expense car) {
//        final EditText editText = new EditText(getContext());
//        editText.setText(ex.getName());
//        editText.setPadding(20, 10, 20, 10);
//        editText.setTextSize(TEXT_SIZE);
//        editText.setSingleLine();
//        editText.addTextChangedListener(new CarNameUpdater(car));
//        return editText;
//    }

    private View renderPrice(final Expense expense) {
        final String priceString = "₹" + PRICE_FORMATTER.format(expense.amount);

        final TextView textView = new TextView(getContext());
        textView.setText(priceString);
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(TEXT_SIZE);

//        if (expense.amount < 50000) {
//            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.));
//        } else if (car.getPrice() > 100000) {
//            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.table_price_high));
//        }

        return textView;
    }

    private View renderDestination(final Expense expense) {
        return renderString(expense.paymentDestination);
    }

    private View renderSource(final Expense expense) {
        return renderString(expense.paymentSource);
    }

//    private View renderProducerLogo(final Car car, final ViewGroup parentView) {
//        final View view = getLayoutInflater().inflate(R.layout.table_cell_image, parentView, false);
//        final ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
//        imageView.setImageResource(car.getProducer().getLogo());
//        return view;
//    }

    private View renderString(final String value) {
        final TextView textView = new TextView(getContext());
        textView.setText(value);
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(TEXT_SIZE);
        return textView;
    }

//    private static class CarNameUpdater implements TextWatcher {
//
//        private Expense expenseToUpdate;
//
//        public CarNameUpdater(Expense expenseToUpdate) {
//            this.expenseToUpdate = expenseToUpdate;
//        }
//
//        @Override
//        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            // no used
//        }
//
//        @Override
//        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            // not used
//        }
//
//        @Override
//        public void afterTextChanged(Editable s) {
//            expenseToUpdate.setName(s.toString());
//        }
//    }
}
