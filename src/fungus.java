import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.WorldHopper;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.local.Health;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.runetek.event.listeners.ItemTableListener;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.ItemTableEvent;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.runetek.providers.RSWorld;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;

import java.awt.*;


@ScriptMeta(name = "fungus",  desc = "Script description", developer = "Developer's Name", category = ScriptCategory.MONEY_MAKING)
public class fungus extends Script implements ItemTableListener, RenderListener {

    private StopWatch timer;
    private int fungusPicked;
    private int tabsUsed;
    private static String FUNGUS = "Mort myre fungus";
    private static String teleTab = "Salve graveyard teleport";
    private static String MUSHROOM_LOG = "Fungi on log";
    private Area outsidePortal = Area.rectangular(3351, 3161,3355, 3166,0);
    private Area teleSpot = Area.rectangular(3423, 3465, 3441, 3455);
    private Position logSpot = new Position(3431, 3433);
    private static String duelRing = "Ring of dueling(8)";



    @Override
    public void onStart()
    {
        fungusPicked = 0;
        tabsUsed = 0;
        timer = StopWatch.start();
    }

    @Override
    public int loop() {
        if(nearClanwars() || insidePortal())
        {
            if(Inventory.contains(FUNGUS))
            {
                bankFungus();
                return 100;
            }
            if(!Inventory.contains(teleTab))
            {
                if(Bank.isClosed())
                {
                    Bank.open();
                    Time.sleepUntil(()-> Bank.isOpen() || !Players.getLocal().isMoving(), 3000);
                    return 100;
                }
                Bank.withdraw(teleTab, 1);
                tabsUsed++;
                Time.sleepUntil(()-> Inventory.contains(teleTab), 3000);
                return 100;
            }
            if(!Equipment.isOccupied(EquipmentSlot.RING))
            {
                if (Inventory.getCount(duelRing) == 0 && Bank.getCount(duelRing) == 0)
                {
                    return -1;
                }
                if(Inventory.getCount(duelRing) > 0)
                {
                    Inventory.getFirst(duelRing).interact("Wear");
                    Time.sleepUntil(()->Equipment.isOccupied(EquipmentSlot.RING),3000);
                    return 100;
                }
                if(Bank.getCount(duelRing) > 0)
                {
                    Bank.withdraw(duelRing,1);
                    Time.sleepUntil(()-> Inventory.getCount(duelRing) > 0, 3000);
                    return 100;
                }
            }
            if(Health.getCurrent() < 8 || Prayers.getPoints() < 40) {

                if (insidePortal()) {
                    Inventory.getFirst(teleTab).click();
                    Time.sleepUntil(()-> nearSwamp(), 1000);
                    return 100;
                }
                SceneObject ffaPortal = SceneObjects.getNearest("Free-for-all portal");
                if (ffaPortal != null) {
                    ffaPortal.interact("Enter");
                    Time.sleepUntil(() -> insidePortal(), 3000);
                    return 100;
                }
                Movement.walkToRandomized(outsidePortal.getCenter());
                Time.sleepUntil(() -> SceneObjects.getNearest("Free-for-all portal") != null, Random.mid(4444, 6666));
                return 100;
            }
            Inventory.getFirst(teleTab).click();
            Time.sleepUntil(()-> nearSwamp(), 500);
            return 100;
        }
        if((Inventory.isFull() || Health.getCurrent() < 4 || Prayers.getPoints() == 0) && !teleSpot.contains(Players.getLocal()))
        {
            if(!Inventory.isFull() && SceneObjects.getNearest(MUSHROOM_LOG) != null)
            {
                SceneObjects.getNearest(MUSHROOM_LOG).click();
                return 100;
            }
            EquipmentSlot.RING.interact("Clan Wars");
            Time.sleepUntil((()-> nearClanwars()), 3000);
            return 100;
        }
        if(nearLogs())
        {
            if(Players.getNearest(a-> a.distance(Players.getLocal()) < 5
                    && !a.getName().equals(Players.getLocal().getName())) != null)
            {
                randomSafeP2P();
                return 100;
            }
            SceneObject mushroom = SceneObjects.getNearest(a-> a.distance(Players.getLocal()) < 5 && a.getName().equals(MUSHROOM_LOG));
            if(mushroom != null)
            {
                int count = Inventory.getCount(FUNGUS);
                mushroom.click();
                Time.sleepUntil(()-> count < Inventory.getCount(FUNGUS), 3000);
                return 100;
            }
            if(Players.getLocal().getPosition().equals(logSpot))
            {
                EquipmentSlot.MAINHAND.interact("Bloom");
                Time.sleepUntil(()-> SceneObjects.getNearest(MUSHROOM_LOG) != null, 1000);
                return 100;
            }
            Movement.walkTo(logSpot);
            toggleRun();
            Time.sleepUntil(()-> Players.getLocal().getPosition().equals(logSpot) || !Players.getLocal().isMoving(), 1000);
            return 100;

        }
        if(!nearSwamp())
        {
            if(Inventory.contains("Silver sickle (b)"))
            {
                Inventory.getFirst("Silver sickle (b)").click();
                return 100;
            }
            if(Equipment.isOccupied(EquipmentSlot.RING))
            {
                EquipmentSlot.RING.interact("Clan Wars");
                Time.sleepUntil(()-> nearClanwars(), 3000);
            }
            if(Inventory.contains(a-> a.getName().contains("Ring of")))
            {
                Inventory.getFirst(a-> a.getName().contains("Ring of")).click();
                return 100;
            }
            if(Bank.isClosed()) {
                Bank.open();
                return 1000;
            }
            if(Bank.isOpen())
            {
                Bank.withdraw(duelRing, 1);
                Time.sleepUntil(()-> Inventory.contains(duelRing), 3000);
                return 100;
            }

        }
        Movement.walkTo(logSpot);
        toggleRun();
        Time.sleepUntil(()-> Players.getLocal().getPosition().equals(logSpot) || !Players.getLocal().isMoving(), 1000);

        return 333;
    }


    @Override
    public void notify(ItemTableEvent e) {
        if(e.getChangeType() == ItemTableEvent.ChangeType.ITEM_ADDED && nearLogs())
        {
            fungusPicked++;
        }
    }

    @Override
    public void notify(RenderEvent renderEvent) {
        Graphics g = renderEvent.getSource();
        g.setColor(new Color(0,0,0,150));
        g.fillRoundRect(5,30,150,130,10,10);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.white);
        g.drawString("Runtime: " + timer.toElapsedString(), 10,50);
        g.drawString("Fungus picked: " + fungusPicked, 10,70);
        g.drawString("Fungus/hr: " + Math.floor(timer.getHourlyRate(fungusPicked)), 10,90);
        g.drawString("Trips/hr: " + Math.floor(timer.getHourlyRate(tabsUsed)), 10,110);

    }


    boolean nearSwamp()
    {
        Position swamp = new Position(3429, 3446);
        if(Players.getLocal().distance(swamp) < 100)
        {
            return true;
        }
        else
            return false;
    }

    boolean nearLogs()
    {
        if(Players.getLocal().distance(logSpot) < 5)
            return true;
        else
            return false;
    }


    boolean nearClanwars()
    {
        Position clanWars = new Position(3366, 3167);
        if(Players.getLocal().distance(clanWars) < 100)
            return true;
        else
            return false;
    }

    boolean insidePortal()
    {
        Position insidePortal = new Position(3327, 4752);
        if(Players.getLocal().distance(insidePortal) < 30)
            return true;
        else
            return false;
    }

    void bankFungus()
    {
        if(Bank.isOpen())
        {
            Bank.depositAll(FUNGUS);
            Time.sleepUntil(()-> !Inventory.contains(FUNGUS), 3000);
        }
        else
        {
            Bank.open(BankLocation.CLAN_WARS);
            Time.sleepUntil(()-> Bank.isOpen(), 3000);
        }
    }

    private boolean toggleRun()
    {
        if (!Movement.isRunEnabled() && Movement.getRunEnergy() > Random.nextInt(10, 30)) { // If our energy is higher than a random value 10-30
            Movement.toggleRun(true); // Toggle run
            return true;
        }
        return false;
    }

    public static boolean randomSafeP2P() {
        if (Bank.isOpen()) {
            Bank.close();
            Time.sleepUntil(() -> Bank.isClosed(), 3000);
        }
        return WorldHopper.randomHop(world -> world.getId() != Game.getClient().getCurrentWorld() && world.isMembers() && !world.isPVP() && !world.isSkillTotal()
                && !world.isTournament() && !world.isHighRisk()
                && !world.isDeadman() && !world.isSeasonDeadman() && world.getLocation() == RSWorld.LOCATION_US
        ); // Check if world is random/safe/p2p
    }




}
