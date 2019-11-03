package mage.cards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;

import mage.Mana;
import mage.ObjectColor;
import mage.cards.repository.CardCriteria;
import mage.cards.repository.CardInfo;
import mage.cards.repository.CardRepository;
import mage.constants.CardType;
import mage.constants.ColoredManaSymbol;
import mage.constants.SuperType;
import mage.filter.FilterMana;
import mage.util.ClassScanner;
import mage.util.RandomUtil;

/**
 * @author BetaSteward_at_googlemail.com, JayDi85
 */
public class Sets extends HashMap<String, ExpansionSet> {

    private static final Logger logger = Logger.getLogger(Sets.class);
    private static final Sets instance = new Sets();

    public static Sets getInstance() {
        return instance;
    }

    private Set<String> customSets = new HashSet<>();

    private Sets() {
        ArrayList<String> packages = new ArrayList<>();
        packages.add("mage.sets");
        for (Class c : ClassScanner.findClasses(null, packages, ExpansionSet.class)) {
            try {
                addSet((ExpansionSet) c.getMethod("getInstance").invoke(null));
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    public void addSet(ExpansionSet set) {
        if (containsKey(set.getCode())) {
            throw new IllegalArgumentException("Set code " + set.getCode() + " already exists.");
        }
        this.put(set.getCode(), set);
        if (set.getSetType().isCustomSet()) {
            customSets.add(set.getCode());
        }
    }

    /**
     * Generates card pool of cardsCount cards that have manacost of allowed
     * colors.
     *
     * @param cardsCount
     * @param allowedColors
     * @return
     */
    public static List<Card> generateRandomCardPool(int cardsCount, List<ColoredManaSymbol> allowedColors) {
        return generateRandomCardPool(cardsCount, allowedColors, false);
    }

    public static List<Card> generateRandomCardPool(int cardsCount, List<ColoredManaSymbol> allowedColors, boolean onlyBasicLands) {
        return generateRandomCardPool(cardsCount, allowedColors, onlyBasicLands, null);
    }

    public static List<Card> generateRandomCardPool(int cardsCount, List<ColoredManaSymbol> allowedColors,
            boolean onlyBasicLands, List<String> allowedSets) {
        CardCriteria criteria = new CardCriteria();

        if (allowedSets != null) {
            for (String code : allowedSets) {
                criteria.setCodes(code);
            }
        }

        List<CardInfo> cards = CardRepository.instance.findCards(criteria);
        if (onlyBasicLands) {
            cards.removeIf(
                    card -> card.getTypes().contains(CardType.LAND) && !card.getSupertypes().contains(SuperType.BASIC));
        }

        if (allowedColors != null) {
            cards.removeIf(card -> {
                ObjectColor color = card.getColor();
                if (!allowedColors.isEmpty() && color.isColorless()) {
                    return true;
                }
                if (color.isWhite() && !allowedColors.contains(ColoredManaSymbol.W)) {
                    return true;
                }
                if (color.isBlue() && !allowedColors.contains(ColoredManaSymbol.U)) {
                    return true;
                }
                if (color.isBlack() && !allowedColors.contains(ColoredManaSymbol.B)) {
                    return true;
                }
                if (color.isRed() && !allowedColors.contains(ColoredManaSymbol.R)) {
                    return true;
                }
                if (color.isGreen() && !allowedColors.contains(ColoredManaSymbol.G)) {
                    return true;
                }
                return false;
            });
        }

        List<Card> cardPool = new ArrayList<>();
        while (cardPool.size() < cardsCount) {
            CardInfo cardInfo = cards.get(RandomUtil.nextInt(cards.size()));
            cardPool.add(cardInfo.getCard());
        }

        return cardPool;
    }

    public static ExpansionSet findSet(String code) {
        if (instance.containsKey(code)) {
            return instance.get(code);
        }
        return null;
    }

}
