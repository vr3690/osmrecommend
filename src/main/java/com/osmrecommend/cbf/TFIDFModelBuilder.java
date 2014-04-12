package com.osmrecommend.cbf;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Maps;
import com.osmrecommend.dao.AreaDAO;

/**
 * Builder for computing {@linkplain TFIDFModel TF-IDF models} from item tag data.  Each item is
 * represented by a normalized TF-IDF vector.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TFIDFModelBuilder implements Provider<TFIDFModel> {
    
	@Autowired
	private AreaDAO dao;

    /**
     * Construct a model builder.  The {@link Inject} annotation on this constructor tells LensKit
     * that it can be used to build the model builder.
     *
     * @param dao The item-tag DAO.  This is where the builder will get access to items and their
     *            tags.
     *            <p>{@link Transient} means that the provider promises that the DAO is no longer
     *            needed once the object is built (that is, the model will not contain a reference
     *            to the DAO).  This allows LensKit to configure your recommender components
     *            properly.  It's up to you to keep this promise.</p>
     */
    @Inject
    public TFIDFModelBuilder(@Transient AreaDAO dao) {
        this.dao = dao;
    }

    /**
     * This method is where the model should actually be computed.
     * @return The TF-IDF model (a model of item tag vectors).
     */
    @Override
    public TFIDFModel get() {
        // Build a map of tags to numeric IDs.  This lets you convert tags (which are strings)
        // into long IDs that you can use as keys in a tag vector.
        Map<String, Long> tagIds = buildTagIdMap();

        // Create a vector to accumulate document frequencies for the IDF computation
        MutableSparseVector docFreq = MutableSparseVector.create(tagIds.values());
        docFreq.fill(0);

        // We now proceed in 2 stages. First, we build a TF vector for each item.
        // While we do this, we also build the DF vector.
        // We will then apply the IDF to each TF vector and normalize it to a unit vector.

        // Create a map to store the item TF vectors.
        Map<Long,MutableSparseVector> itemVectors = Maps.newHashMap();

        // Create a work vector to accumulate each item's tag vector.
        // This vector will be re-used for each item.
        MutableSparseVector work = MutableSparseVector.create(tagIds.values());

        // Iterate over the items to compute each item's vector.
        LongSet items = dao.getItemIds();
        for (long item: items) {
            // Reset the work vector for this item's tags.
            work.clear();
            // Now the vector is empty (all keys are 'unset').

            // Populate the work vector with the number of times each tag is applied to this item.
            // First, get the list of tags for an item.
            List<String> itemTags=dao.getItemTags(item);
            // Iterate over this list.
            for(String tag: itemTags) {
            	// Fetch the numeric Id for this tag.
            	long tagId=tagIds.get(tag);
            	// Get the current count for this tag.
            	double currentCount=0;
            	if(work.containsKey(tagId)) {
            		currentCount=work.get(tagId);
            	}
            	//Increment the count.
            	currentCount++;
            	//set it.
            	work.set(tagId, currentCount);
            }
            // Increment the document frequency vector once for each unique tag on the item.
            // Get the set of tags for this item.
            Set<String> uniqueItemTags=new HashSet<String>(itemTags);
            // Iterate over this set.
            for(String tag: uniqueItemTags) {
            	// Get the numeric Id for this tag.
            	long tagId=tagIds.get(tag);
            	// Get the count of this tag
            	double currentCount=0;
            	if(work.containsKey(tagId)) {
            		currentCount=docFreq.get(tagId);
            	}
            	// Increment the count.
            	currentCount++;
            	// Set the count.
            	docFreq.set(tagId, currentCount);
            }
            // Save a shrunk copy of the vector (only storing tags that apply to this item) in
            // our map, we'll add IDF and normalize later.
            itemVectors.put(item, work.shrinkDomain());
            // work is ready to be reset and re-used for the next item
        }

        // Now we've seen all the items, so we have each item's TF vector and a global vector
        // of document frequencies.
        // Invert and log the document frequency.  We can do this in-place.
        for (VectorEntry e: docFreq.fast()) {
            // Update this document frequency entry to be a log-IDF value
        	double freq=e.getValue();
        	freq=freq/items.size();
        	freq=Math.log(freq);
        	docFreq.set(e, freq);
        }

        // Now docFreq is a log-IDF vector.
        // So we can use it to apply IDF to each item vector to put it in the final model.
        // Create a map to store the final model data.
        Map<Long,SparseVector> modelData = Maps.newHashMap();
        for (Map.Entry<Long,MutableSparseVector> entry: itemVectors.entrySet()) {
            MutableSparseVector tv = entry.getValue();
            // TODO Convert this vector to a TF-IDF vector
            for(long tagId: tv.keySet()) {
            	double tf=tv.get(tagId);
            	double tfidf=tf*docFreq.get(tagId);
            	tv.set(tagId, tfidf);
            }
            // TODO Normalize the TF-IDF vector to be a unit vector
            // HINT The method tv.norm() will give you the Euclidian length of the vector
            double length=tv.norm();
            for (long tagId: tv.keySet()) {
            	tv.set(tagId, tv.get(tagId)/length);
            }
            // Store a frozen (immutable) version of the vector in the model data.
            modelData.put(entry.getKey(), tv.freeze());
        }

        // we technically don't need the IDF vector anymore, so long as we have no new tags
        return new TFIDFModel(tagIds, modelData);
    }

    /**
     * Build a mapping of tags to numeric IDs.
     *
     * @return A mapping from tags to IDs.
     */
    private Map<String,Long> buildTagIdMap() {
        // Get the universe of all tags
        ObjectSet<String> tags = dao.getTagVocabulary();
        // Allocate our new tag map
        Map<String,Long> tagIds = Maps.newHashMap();

        for (String tag: tags) {
            // Map each tag to a new number.
            tagIds.put(tag, tagIds.size() + 1L);
        }
        return tagIds;
    }
}
