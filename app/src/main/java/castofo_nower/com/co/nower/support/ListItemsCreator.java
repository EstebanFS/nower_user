package castofo_nower.com.co.nower.support;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import castofo_nower.com.co.nower.R;
import castofo_nower.com.co.nower.controllers.BranchesListFragment;
import castofo_nower.com.co.nower.controllers.NowerMap;
import castofo_nower.com.co.nower.controllers.UserPromosListFragment;
import castofo_nower.com.co.nower.models.Branch;
import castofo_nower.com.co.nower.models.MapData;
import castofo_nower.com.co.nower.models.Redemption;

public class ListItemsCreator extends ArrayAdapter<Object> implements Filterable
{

  private Context context;
  private int resource;
  private ArrayList<Object> listData;
  private String action;

  // Copia de los datos de la lista original.
  private ArrayList<Object> originalList;

  public ListItemsCreator(Context context, int resource,
                          ArrayList<Object> promosList, String action) {
    super(context, resource, promosList);
    this.context = context;
    this.resource = resource;
    this.listData = promosList;
    this.originalList = cloneListData(listData);
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

    // Se cambia el tipo de letra para Roboto (Según especificaciones de
    // Material Design)
    Typeface customFont = Typeface
            .createFromAsset(context.getAssets(),"fonts/Roboto-Regular.ttf");
    title.setTypeface(customFont);
    subtitle.setTypeface(customFont);

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
          // Los encabezados debe tener Roboto Medium según especificaciones de
          // Material Design
          Typeface customFontHeader = Typeface
              .createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");
          title.setTypeface(customFontHeader);
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
          disableClicking(item);
        }
        // Se trata de un item.
        else {
          int promoId = redemption.getPromoId();
          if (promoId != SearchHandler.NO_RESULTS_FOUND) {
            try {
              titleText = MapData.getPromosMap().get(promoId).getTitle();
            }
            catch (Exception e) {

            }
            subtitleText = redemption.getStoreName();
          }
          // Se está formando un item para indicar que no se encontraron
          // resultados al filtrar la lista de promociones del usuario.
          else {
            // En este caso, el mensaje a mostrar fue guardado en el campo
            // storeName de la promoción del usuario creada.
            titleText = redemption.getStoreName();
          }
          // Se le pone el id de la promoción a redimir con el fin de poder
          // gestionarla al ser presionada por el usuario.
          item.setId(promoId);

          setNoResultsItem(item, title, subtitle, icon);

          // Se recupera el logo de la tienda.
          if (redemption.getStoreLogoURL() != null) {
            ImageDownloader imageDownloader
            = new ImageDownloader(icon, redemption.getStoreLogoURL());
            imageDownloader.execute();
          }

          // Se esconde el indicador de cantidad de promociones.
          View indicator = item.findViewById(R.id.promos_counter_indicator);
          indicator.setVisibility(View.GONE);
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

        setNoResultsItem(item, title, subtitle, icon);

        // Se recupera el logo de la tienda.
        if (b.getStoreLogoURL() != null) {
          ImageDownloader imageDownloader
          = new ImageDownloader(icon, b.getStoreLogoURL());
          imageDownloader.execute();
        }

        // Se establece el número de promociones que posee la sucursal en el
        // indicador.
        TextView promosCounter = (TextView) item
                .findViewById(R.id.promos_counter);
        int promosInBranch = b.getPromosIds().size();
        promosCounter.setText(NowerMap.formatPromosCount(promosInBranch));
        break;
    }

    title.setText(titleText);
    subtitle.setText(subtitleText);

    //Se retorna el item que va a ser mostrado para cada fila de la lista.
    return item;
  }

  public ArrayList<Object> cloneListData(ArrayList<Object> listData) {
    ArrayList<Object> clonedData = new ArrayList<>();
    clonedData.addAll(listData);
    return clonedData;
  }

  public void disableClicking(View item) {
    item.setEnabled(false);
    item.setOnClickListener(null);
  }

  public void setNoResultsItem(View item, TextView title, TextView subtitle,
                               ImageView icon ) {
    // Este caso se presenta cuando no se han encontrado resultados al filtrar
    // las listas.
    if (item.getId() == SearchHandler.NO_RESULTS_FOUND) {
      title.setTypeface(null, Typeface.BOLD);
      title.setGravity(Gravity.CENTER);
      subtitle.setVisibility(View.GONE);
      icon.setVisibility(View.GONE);
      disableClicking(item);
    }
  }

  public void updateListData(ArrayList<Object> newData, boolean setAsOriginal) {
    this.listData.clear();
    this.listData.addAll(newData);
    if (setAsOriginal) {
      this.originalList.clear();
      this.originalList.addAll(newData);
    }
    notifyDataSetChanged();
  }

  @Override
  public Filter getFilter() {
    return new ListItemsFilter();
  }

  private class ListItemsFilter extends Filter {

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      FilterResults filterResults = new FilterResults();
      if (constraint != null && constraint.length() > 0) {
        ArrayList<Object> matchingItems = new ArrayList<Object>();
        switch (action) {
          case UserPromosListFragment.LIST_USER_PROMOS:
            matchingItems.addAll(filterUserPromos(constraint.toString()));
            break;
          case BranchesListFragment.LIST_BRANCHES:
            matchingItems.addAll(filterBranches(constraint.toString()));
            break;
        }
        filterResults.count = matchingItems.size();
        filterResults.values = matchingItems;
      }
      else {
        filterResults.count = originalList.size();
        filterResults.values = originalList;
      }

      return filterResults;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence constraint,
                                  FilterResults results) {
      if (results.count == 0) {
        ArrayList<Object> noResults = new ArrayList<>();
        Object noResultsItem = new Object();
        switch (action) {
          case UserPromosListFragment.LIST_USER_PROMOS:
            noResultsItem = setNoRedemptionFoundItem();
            break;
          case BranchesListFragment.LIST_BRANCHES:
            noResultsItem = setNoBranchFoundItem();
            break;
        }
        noResults.add(noResultsItem);
        results.count = noResults.size();
        results.values = noResults;
      }
      updateListData((ArrayList<Object>) results.values, false);
    }

    public ArrayList<Object> filterBranches(String constraint) {
      ArrayList<Object> matchingItems = new ArrayList<>();
      String storeName, name, stdConstraint;
      for (Object b : originalList) {
        Branch branch = (Branch) b;
        storeName = standardizeWord(branch.getStoreName());
        name = standardizeWord(branch.getName());
        stdConstraint = standardizeWord(constraint);
        if (storeName.contains(stdConstraint) || name.contains(stdConstraint)) {
          matchingItems.add(b);
        }
      }

      return matchingItems;
    }

    public ArrayList<Object> filterUserPromos(String constraint) {
      ArrayList<Object> userPromosNotRedeemed = new ArrayList<Object>();
      ArrayList<Object> userPromosRedeemed = new ArrayList<Object>();
      ArrayList<Object> matchingItems = new ArrayList<>();

      // Encabezados "POR REDIMIR" y "REDIMIDAS".
      userPromosNotRedeemed.add(new Redemption("0", 0, false, null, null));
      userPromosRedeemed.add(new Redemption("1", 0, true, null, null));

      String promoTitle, storeName, stdConstraint;
      for (Object r : originalList) {
        Redemption redemption = (Redemption) r;
        if (!redemption.getCode().equals("0")
            && !redemption.getCode().equals("1")) {
          int promoId = redemption.getPromoId();
          promoTitle = standardizeWord(MapData.getPromosMap()
                                       .get(promoId).getTitle());
          storeName = standardizeWord(redemption.getStoreName());
          stdConstraint = standardizeWord(constraint);
          if (promoTitle.contains(stdConstraint)
              || storeName.contains(stdConstraint)) {
            if (!redemption.isRedeemed()) userPromosNotRedeemed.add(redemption);
            else userPromosRedeemed.add(redemption);
          }
        }
      }
      // Solamente se agregan los encabezados cuando existe por lo menos alguna
      // coincidencia con las promociones del usuario en esa sección.
      if (userPromosNotRedeemed.size() > 1) {
        matchingItems.addAll(userPromosNotRedeemed);
      }
      if (userPromosRedeemed.size() > 1) {
        matchingItems.addAll(userPromosRedeemed);
      }

      return matchingItems;
    }

    public String standardizeWord(String word) {
      String wordWithoutAccents = word.replaceAll("á", "a").replaceAll("é", "e")
                                  .replaceAll("í", "i").replaceAll("ó", "o")
                                  .replaceAll("ú", "u");
      wordWithoutAccents = wordWithoutAccents.replaceAll("[^a-zA-Z0-9 ]", "");

      return wordWithoutAccents.toLowerCase();
    }

    public Branch setNoBranchFoundItem() {
      Branch noBranchFound
      = new Branch(SearchHandler.NO_RESULTS_FOUND, null, -1.0, -1.0, -1,
                   context.getResources().getString(R.string.no_results_found),
                   null, null);

      return noBranchFound;
    }

    public Redemption setNoRedemptionFoundItem() {
      Redemption noRedemptionFound
      = new Redemption(String.valueOf(SearchHandler.NO_RESULTS_FOUND),
                       SearchHandler.NO_RESULTS_FOUND, false,
                       context.getResources()
                       .getString(R.string.no_results_found), null);

      return noRedemptionFound;
    }
  }
}
