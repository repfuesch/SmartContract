package smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.list;

/**
 * Created by flo on 19.07.17.
 */

public class ListSearch {

    private String searchText;

    public ListSearch(String searchText)
    {
        setSearchText(searchText);
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
}
