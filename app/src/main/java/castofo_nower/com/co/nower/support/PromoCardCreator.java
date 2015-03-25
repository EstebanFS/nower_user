package castofo_nower.com.co.nower.support;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.controllers.PromoCardAnimator;


public class PromoCardCreator {

    private Context context;

    public PromoCardCreator(Context context) {
        this.context = context;
    }

    public void showPromoCard(String title, String expirationDate, int availableRedemptions) {

        // Se crea la tarjeta de la promoción.
        final Dialog promoCard = new Dialog(context);

        // Se cambia la presentación del diálogo por el propio diseño.
        promoCard.setContentView(R.layout.promo_card);

        promoCard.setTitle(title);
        /*
        TextView txt = (TextView) promoCard.findViewById(R.id.txt);

        txt.setText("Expira en: " + expirationDate + ", Disponibles: " + availableRedemptions);

        Button dialogButton = (Button) promoCard.findViewById(R.id.dialogButton);

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(context, PromoCardAnimator.class);
                context.startActivity(a);
            }
        });
        */

        promoCard.show();
    }
}
