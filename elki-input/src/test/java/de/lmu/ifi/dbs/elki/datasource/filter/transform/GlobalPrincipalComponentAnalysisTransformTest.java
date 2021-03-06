/*
 * This file is part of ELKI:
 * Environment for Developing KDD-Applications Supported by Index-Structures
 * 
 * Copyright (C) 2017
 * ELKI Development Team
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.lmu.ifi.dbs.elki.datasource.filter.transform;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.datasource.AbstractDataSourceTest;
import de.lmu.ifi.dbs.elki.datasource.bundle.MultipleObjectsBundle;
import de.lmu.ifi.dbs.elki.math.MeanVariance;
import de.lmu.ifi.dbs.elki.math.linearalgebra.CovarianceMatrix;
import de.lmu.ifi.dbs.elki.utilities.ELKIBuilder;

/**
 * Test the PCA transformation filter.
 *
 * @author Matthew Arcifa
 */
public class GlobalPrincipalComponentAnalysisTransformTest extends AbstractDataSourceTest {
  /**
   * Test with default parameters.
   */
  @Test
  public void defaultParameters() {
    String filename = UNITTEST + "transformation-test-1.csv";
    GlobalPrincipalComponentAnalysisTransform<DoubleVector> filter = new ELKIBuilder<GlobalPrincipalComponentAnalysisTransform<DoubleVector>>(GlobalPrincipalComponentAnalysisTransform.class).build();
    MultipleObjectsBundle bundle = readBundle(filename, filter);
    int dim = getFieldDimensionality(bundle, 0, TypeUtil.NUMBER_VECTOR_FIELD);

    // We verify that the result has mean 0 and variance 1 in each column.
    // We also expect that covariances of any two columns are 0.
    CovarianceMatrix cm = new CovarianceMatrix(dim);
    MeanVariance[] mvs = MeanVariance.newArray(dim);
    for(int row = 0; row < bundle.dataLength(); row++) {
      DoubleVector d = get(bundle, row, 0, DoubleVector.class);
      cm.put(d);
      for(int col = 0; col < dim; col++) {
        final double v = d.doubleValue(col);
        if(v > Double.NEGATIVE_INFINITY && v < Double.POSITIVE_INFINITY) {
          mvs[col].put(v);
        }
      }
    }
    double[][] ncm = cm.destroyToPopulationMatrix();
    for(int col = 0; col < dim; col++) {
      for(int row = 0; row < dim; row++) {
        assertEquals("Unexpected covariance", col == row ? 1. : 0., ncm[row][col], 1e-15);
      }
      assertEquals("Mean not as expected", 0., mvs[col].getMean(), 1e-15);
      assertEquals("Variance not as expected", 1., mvs[col].getNaiveVariance(), 1e-15);
    }
  }
}
