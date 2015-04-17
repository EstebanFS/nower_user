package castofo_nower.com.co.nower.support;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.controllers.BranchesList;
import castofo_nower.com.co.nower.controllers.UserPromoList;
import castofo_nower.com.co.nower.models.Branch;
import castofo_nower.com.co.nower.models.MapData;
import castofo_nower.com.co.nower.models.Redemption;


public class ListItemsCreator extends ArrayAdapter<Object> {

  private final Context context;
  private final int resource;
  private final ArrayList<Object> listData;
  private final String action;

  public ListItemsCreator(Context context, int resource,
                          ArrayList<Object> promosList, String action) {
    super(context, resource, promosList);
    this.context = context;
    this.resource = resource;
    this.listData = promosList;
    this.action = action;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    LayoutInflater inflater = (LayoutInflater) context
                              .getSystemService
                              (Context.LAYOUT_INFLATER_SERVICE);

    View item = inflater.inflate(resource, parent, false);

    TextView title = (TextView) item.findViewById(R.id.title);
    TextView subtitle = (TextView) item.findViewById(R.id.subtitle);
    ImageView icon = (ImageView) item.findViewById(R.id.icon);

    String titleText = "";
    String subtitleText = "";
    Drawable iconImg = null;
    switch (action) {
      case UserPromoList.LIST_USER_PROMOS:
        // Se obtiene el título de la promoción actual.
        Redemption redemption = (Redemption) listData.get(position);

        // Se trata de un encabezado de sección.
        if (redemption.getCode().equals("0")
            || redemption.getCode().equals("1")) {
          item = inflater.inflate(R.layout.redeemed_status_header, parent,
                                  false);
          title = (TextView) item.findViewById(R.id.redeemed_status_title);
          // Se trata del encabezado de promociones no redimidas.
          if (redemption.getCode().equals("0")) {
            titleText = context.getResources().getString(R.string.not_redeemed);
          }
          // Se trata del encabezado de promoiones redimidas.
          else {
            titleText = context.getResources().getString(R.string.redeemed);
          }
          // Es un encabezado y por tanto no debe hacerse nada cuando el
          // usuario lo presiona.
          item.setId(UserPromoList.HEADER_ID);
        }
        // Se trata de un item.
        else {
          int promoId = redemption.getPromoId();
          titleText = MapData.getPromosMap().get(promoId).getTitle();
          subtitleText = redemption.getStoreName();
          // Se le pone el id de la promoción a redimir con el fin de poder
          // gestionarla al ser presionada por el usuario.
          item.setId(promoId);
          iconImg = context.getResources().getDrawable(R.drawable.promo_icon);
        }
        break;
      case BranchesList.LIST_BRANCHES:
        // Se obtiene el nombre del establecimiento actual.
        Branch b = (Branch) listData.get(position);
        // El título a mostrar incluye, además del nombre del establecimiento,
        // su sucursal.
        titleText = b.getStoreName();
        subtitleText = b.getName();
        // Se le pone el ID del establecimiento para poder gestionarlo al ser
        // presionado por el usuario.
        item.setId(b.getId());
        iconImg = context.getResources().getDrawable(R.drawable.nower_marker);
        break;
    }

    title.setText(titleText);
    subtitle.setText(subtitleText);

    if (iconImg != null) icon.setImageDrawable(iconImg);

        /*
        Drawable categoryIcon = null;
        switch(itemsArrayList.get(position).getCategory()){
            case "biblioteca":
                categoryIcon = context.getResources().getDrawable(R.drawable.library);
                break;
            case "bloque":
                categoryIcon = context.getResources().getDrawable(R.drawable.block);
                break;
            case "auditorio":
                categoryIcon = context.getResources().getDrawable(R.drawable.auditorium);
                break;
            case "idiomas":
                categoryIcon = context.getResources().getDrawable(R.drawable.language_center);
                break;
            case "cec":
                categoryIcon = context.getResources().getDrawable(R.drawable.cec);
                break;
            case "portería":
                categoryIcon = context.getResources().getDrawable(R.drawable.entrance);
                break;
            case "no resultados":
                categoryIcon = context.getResources().getDrawable(R.drawable.no_results);
                break;
            case "nueva nota":
                categoryIcon = context.getResources().getDrawable(R.drawable.new_note);
                break;
            case "marcador usuario":
                categoryIcon = context.getResources().getDrawable(R.drawable.user_marker);
                break;
            case "nota usuario":
                categoryIcon = context.getResources().getDrawable(R.drawable.user_note);
                break;

            default:
                break;
        }
        categoryView.setImageDrawable(categoryIcon);
        */

    //Se retorna el item que va a ser mostrado para cada fila de la lista.
    return item;
  }

}
