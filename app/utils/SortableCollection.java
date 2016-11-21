/*! LICENSE
 *
 * Copyright (c) 2015, The Agile Factory SA and/or its affiliates. All rights
 * reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import utils.SortableCollection.ISortableObject;

/**
 * A "date sortable collection" is used to sort a collection of objects provided
 * by different classes.
 * 
 * @param <T>
 *            the sortable object type
 * 
 * @author Johann Kohler
 * 
 */
public class SortableCollection<T extends ISortableObject> {

    private List<T> objects;

    /**
     * Default constructor.
     */
    public SortableCollection() {
        objects = new ArrayList<>();
    }

    /**
     * Add a sortable object.
     * 
     * @param object
     *            the sortable object
     */
    public void addObject(T object) {
        this.objects.add(object);
    }

    /**
     * Get the objects sorted.
     */
    public List<T> getSorted() {
        Collections.sort(objects);
        return objects;
    }

    /**
     * A ISortableObject is an element to compare with each other in order to
     * sort it.
     * 
     * @author Johann Kohler
     * 
     */
    public interface ISortableObject extends Comparable<ISortableObject> {

        /**
         * Get the object.
         */
        Object getObject();

        /**
         * Return the value of the sortable attributes as a string.
         */
        String getSortableAttributesAsString();

    }

    /**
     * A DateSortableObject is used to sort objects thanks a single date.
     * 
     * @author Johann Kohler
     * 
     */
    public static class DateSortableObject implements ISortableObject {

        private Object object;

        /* sort attributes */
        private Date date;

        /**
         * Construct a date sortable object.
         * 
         * @param date
         *            the date
         * @param object
         *            the object
         */
        public DateSortableObject(Date date, Object object) {
            this.date = date;
            this.object = object;
        }

        @Override
        public int compareTo(ISortableObject sortableObject) {
            DateSortableObject dateSortableObject = (DateSortableObject) sortableObject;
            if (this.date == null) {
                return -1;
            }
            if (dateSortableObject.date == null) {
                return 1;
            }
            return this.date.compareTo(dateSortableObject.date);
        }

        @Override
        public Object getObject() {
            return this.object;
        }

        @Override
        public String getSortableAttributesAsString() {
            return "[date: " + this.date + "]";
        }
        public Date getDate() {
            return this.date;
        }

    }

    /**
     * A ComplexSortableObject is used to sort objects thanks a date and 2
     * priority attributes.
     * 
     * @author Johann Kohler
     * 
     */
    public static class ComplexSortableObject implements ISortableObject {

        private Object object;

        /* sort attributes */
        private Date date;
        private int priority1;
        private int priority2;

        /**
         * Construct a complex sortable object.
         * 
         * @param date
         *            the date
         * @param priority1
         *            the first priority value
         * @param priority2
         *            the second priority value
         * @param object
         *            the object
         */
        public ComplexSortableObject(Date date, int priority1, int priority2, Object object) {
            this.date = date;
            this.priority1 = priority1;
            this.priority2 = priority2;
            this.object = object;
        }

        @Override
        public int compareTo(ISortableObject sortableObject) {
            ComplexSortableObject complexSortableObject = (ComplexSortableObject) sortableObject;

            // if the dates are equal
            if ((this.date == null && complexSortableObject.date == null)
                    || (this.date != null && complexSortableObject.date != null && this.date.equals(complexSortableObject.date))) {

                // if the first priorities are equal
                if (this.priority1 == complexSortableObject.priority1) {
                    return this.priority2 - complexSortableObject.priority2;

                } else { // the first priorities are not equal
                    return this.priority1 - complexSortableObject.priority1;
                }

            } else { // the dates are not equal

                if (this.date == null) {
                    return -1;
                }
                if (complexSortableObject.date == null) {
                    return 1;
                }
                return this.date.compareTo(complexSortableObject.date);
            }
        }

        @Override
        public Object getObject() {
            return this.object;
        }

        @Override
        public String getSortableAttributesAsString() {
            return "[date: " + this.date + ", priority1: " + this.priority1 + ", priority2: " + this.priority2 + "]";
        }

    }

}
