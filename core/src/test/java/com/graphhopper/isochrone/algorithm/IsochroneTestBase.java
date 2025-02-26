package com.graphhopper.isochrone.algorithm;

import com.graphhopper.json.Statement;
import com.graphhopper.routing.Router;
import com.graphhopper.routing.RouterConfig;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValueImpl;
import com.graphhopper.routing.ev.SimpleBooleanEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.TurnCostProvider;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.routing.weighting.custom.CustomModelParser;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.GHUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static com.graphhopper.json.Statement.If;

public class IsochroneTestBase {
    protected final BooleanEncodedValue accessEnc = new SimpleBooleanEncodedValue("access", true);
    protected final DecimalEncodedValue speedEnc = new DecimalEncodedValueImpl("speed", 5, 5, false);
    protected final BooleanEncodedValue ferryEnc = new SimpleBooleanEncodedValue("ferry", false);
    protected final EncodingManager encodingManager = EncodingManager.start().add(accessEnc).add(speedEnc).add(ferryEnc).build();
    protected BaseGraph graph;

    protected Weighting createWeighting() {
        return createWeighting(TurnCostProvider.NO_TURN_COST_PROVIDER);
    }

    protected Weighting createWeighting(TurnCostProvider turnCostProvider) {
        return CustomModelParser.createWeighting(encodingManager, turnCostProvider, createBaseCustomModel());
    }

    protected CustomModel createBaseCustomModel() {
        CustomModel customModel = new CustomModel();
        customModel.addToPriority(If("!" + accessEnc.getName(), Statement.Op.MULTIPLY, "0"));
        customModel.addToSpeed(If("true", Statement.Op.LIMIT, speedEnc.getName()));
        return customModel;
    }

    @BeforeEach
    public void setUp() {
        graph = new BaseGraph.Builder(encodingManager).create();
    }

    @AfterEach
    public void tearDown() {
        graph.close();
    }
}
