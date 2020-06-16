package facades;

import com.google.gson.Gson;
import dto.IngredientDTO;
import dto.IngredientsDTO;
import dto.MenuPlanDTO;
import dto.MenuPlansDTO;
import dto.RecipeDTO;
import entities.DayPlan;
import entities.Ingredient;
import entities.MenuPlan;
import entities.User;
import errorhandling.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class MenuPlanFacade {
    
    private static MenuPlanFacade instance;
    private static EntityManagerFactory emf;

    /**
     *
     * @param _emf
     * @return an instance of this facade class.
     */
    public static MenuPlanFacade getMenuPlanFacade(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new MenuPlanFacade();
        }
        return instance;
    }
    
    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    private static final Gson GSON = new Gson();
    private static final RecipeFacade recipeFacade = RecipeFacade.getRecipeFacade(emf);
    
    public MenuPlanDTO newMenuPlan(String username, String menuName) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            User user = em.find(User.class, username);
            MenuPlan newMenuPlan = new MenuPlan(user, menuName);
            em.persist(newMenuPlan);
            if (!user.getMenuPlans().contains(newMenuPlan)) {
                user.getMenuPlans().add(newMenuPlan);
            }
            em.getTransaction().commit();
            return new MenuPlanDTO(newMenuPlan);
        } finally {
            em.close();
        }
    }
    
    public void DeleteMenuPlan(String username, int menuPlanID) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            User user = em.find(User.class, username);
            MenuPlan menuPlan = em.find(MenuPlan.class, menuPlanID);
            if (Objects.nonNull(menuPlan)) {
                if (user.getMenuPlans().contains(menuPlan)) {
                    user.getMenuPlans().remove(menuPlan);
                }
                em.remove(menuPlan);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    
    public MenuPlanDTO addDayPlan(int menuPlanID, int numberOfServings, RecipeDTO recipe) throws NotFoundException {
        if (Objects.nonNull(recipe.getId()) && Objects.nonNull(recipe.getIngredient_list())) {

            //Counting up the amount of ingredients from the external recipe
            List<Ingredient> newIngredients = new ArrayList();
            for (IngredientDTO ingredientDTO : recipe.getIngredient_list()) {
                newIngredients.add(new Ingredient(ingredientDTO, numberOfServings));
            }
            
            EntityManager em = getEntityManager();
            try {
                em.getTransaction().begin();
                //adding the dayplan to the menuplan
                MenuPlan menuPlan = em.find(MenuPlan.class, menuPlanID);
                DayPlan newDayPlan = new DayPlan(recipe.getId(), recipe.getName(), numberOfServings);
                em.persist(newDayPlan);
                if (!menuPlan.getDayPlans().contains(newDayPlan)) {
                    menuPlan.getDayPlans().add(newDayPlan);
                }
                if (!Objects.equals(menuPlan, newDayPlan.getMenuPlan())) {
                    newDayPlan.setMenuPlan(menuPlan);
                }
                
                for (Ingredient newIngredient : newIngredients) {
                    boolean matchFound = false;
                    for (Ingredient ingredient : menuPlan.getShoppingList()) {
                        if (ingredient.getName().equals(newIngredient.getName())) {
                            matchFound = true;
                            int oldAmount = ingredient.getAmount();
                            ingredient.setAmount(oldAmount + newIngredient.getAmount());
                            break;
                        }
                    }
                    if (!matchFound) {
                        em.persist(newIngredient);
                        if (!menuPlan.getShoppingList().contains(newIngredient)) {
                            menuPlan.getShoppingList().add(newIngredient);
                        }
                        if (!Objects.equals(newIngredient.getMenuPlan(), menuPlan)) {
                            newIngredient.setMenuPlan(menuPlan);
                        }
                    }
                }
                
                em.getTransaction().commit();
                return new MenuPlanDTO(menuPlan);
            } finally {
                em.close();
            }
        } else {
            throw new NotFoundException("The Recipe could not be fetched. Either the server is down, or nothing was found!");
        }
    }
    
    public MenuPlansDTO getMenuPlans(String username) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            User user = em.find(User.class, username);
            List<MenuPlan> result = user.getMenuPlans();
            em.getTransaction().commit();
            return MenuPlansDTO.getMenuPlansDTO(result);
        } finally {
            em.close();
        }
    }
    
    public MenuPlansDTO getAllMenuPlans() {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            List<MenuPlan> result = em.createQuery("Select m from MenuPlan m", MenuPlan.class).getResultList();
            em.getTransaction().commit();
            return MenuPlansDTO.getMenuPlansDTO(result);
        } finally {
            em.close();
        }
    }
    
}
