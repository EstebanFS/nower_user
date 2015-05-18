package castofo_nower.com.co.nower.support;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.controllers.BranchesListFragment;
import castofo_nower.com.co.nower.controllers.UserPromosListFragment;
import castofo_nower.com.co.nower.models.Branch;
import castofo_nower.com.co.nower.models.MapData;
import castofo_nower.com.co.nower.models.Promo;
import castofo_nower.com.co.nower.models.Redemption;


public class ListItemsCreator extends ArrayAdapter<Object> {

  private Context context;
  private int resource;
  private ArrayList<Object> listData;
  private String action;

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

    switch (action) {
      case UserPromosListFragment.LIST_USER_PROMOS:
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
          item.setId(UserPromosListFragment.HEADER_ID);
        }
        // Se trata de un item.
        else {
          int promoId = redemption.getPromoId();
          Map<Integer, Promo> promosMap = null;
          Promo p = null;
          try {
            promosMap = MapData.getPromosMap();
            p = promosMap.get(promoId);
            titleText = p.getTitle();
            //titleText = MapData.getPromosMap().get(promoId).getTitle();
          }
          catch (Exception e) {

          }
          subtitleText = redemption.getStoreName();
          // Se le pone el id de la promoción a redimir con el fin de poder
          // gestionarla al ser presionada por el usuario.
          item.setId(promoId);
          // Se recupera el logo de la tienda.
          if (redemption.getStoreLogoURL() != null) {
            ImageDownloader imageDownloader
            = new ImageDownloader(icon, redemption.getStoreLogoURL());
            imageDownloader.execute();
          }
        }
        break;
      case BranchesListFragment.LIST_BRANCHES:
        // Se obtiene el nombre del establecimiento actual.
        Branch b = (Branch) listData.get(position);
        // El título a mostrar incluye, además del nombre del establecimiento,
        // su sucursal.
        titleText = b.getStoreName();
        subtitleText = b.getName();
        // Se le pone el ID del establecimiento para poder gestionarlo al ser
        // presionado por el usuario.
        item.setId(b.getId());

        // Se recupera el logo de la tienda.
        if (b.getStoreLogoURL() != null) {
          ImageDownloader imageDownloader
          = new ImageDownloader(icon, b.getStoreLogoURL());
          imageDownloader.execute();
        }
        break;
    }

    title.setText(titleText);
    subtitle.setText(subtitleText);

    //Se retorna el item que va a ser mostrado para cada fila de la lista.
    return item;
  }

  public void updateListData(ArrayList<Object> newData) {
    this.listData.clear();
    this.listData.addAll(newData);
    notifyDataSetChanged();
  }
}
