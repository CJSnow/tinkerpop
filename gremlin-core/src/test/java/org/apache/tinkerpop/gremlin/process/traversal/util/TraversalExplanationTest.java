/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.tinkerpop.gremlin.process.traversal.util;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TraversalExplanationTest {

    @Test
    public void shouldSupportAnonymousTraversals() {
        final String toString = __.out("knows").in("created").explain().toString();
        assertTrue(toString.contains("Traversal Explanation"));
        assertTrue(toString.contains("Original Traversal"));
        assertTrue(toString.contains("Final Traversal"));
    }

    @Test
    public void shouldApplyStrategiesCorrectly() {
        Traversal.Admin<?, ?> traversal = __.out().count().asAdmin();
        traversal.setStrategies(TraversalStrategies.GlobalCache.getStrategies(Graph.class));
        int found = 0;
        for (final String line : traversal.explain().toString().split("\n")) {
            if (line.contains("AdjacentToIncidentStrategy") && line.contains("[VertexStep(OUT,edge)"))
                found++;
        }
        assertEquals(1, found);
        ///
        traversal = __.out().group().by(__.in().count()).asAdmin();
        traversal.setStrategies(TraversalStrategies.GlobalCache.getStrategies(Graph.class).clone());
        found = 0;
        for (final String line : traversal.explain().toString().split("\n")) {
            if (line.contains("AdjacentToIncidentStrategy") && line.contains("[VertexStep(IN,edge)"))
                found++;
        }
        assertEquals(1, found);
        ///
        traversal = __.outE().inV().group().by(__.inE().outV().groupCount().by(__.both().count().is(P.gt(2)))).asAdmin();
        traversal.setStrategies(TraversalStrategies.GlobalCache.getStrategies(Graph.class).clone());
        found = 0;
        for (final String line : traversal.explain().toString().split("\n")) {
            if (line.contains("IncidentToAdjacentStrategy") && line.contains("[VertexStep(IN,vertex)"))
                found++;
            if (line.contains("IncidentToAdjacentStrategy") && line.contains("[VertexStep(OUT,vertex)"))
                found++;
            if (line.contains("AdjacentToIncidentStrategy") && line.contains("[VertexStep(BOTH,edge)"))
                found++;
            if (line.contains("RangeByIsCountStrategy") && line.contains("RangeGlobalStep(0,3)"))
                found++;
        }
        assertEquals(4, found);
        // System.out.println(traversal.explain());
    }
}
