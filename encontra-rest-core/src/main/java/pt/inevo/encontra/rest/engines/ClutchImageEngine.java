package pt.inevo.encontra.rest.engines;

import pt.inevo.encontra.common.DefaultResultProvider;
import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.descriptors.Descriptor;
import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.engine.SimpleIndexedObjectFactory;
import pt.inevo.encontra.image.descriptors.ColorLayoutDescriptor;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.search.AbstractSearcher;
import pt.inevo.encontra.index.search.SimpleSearcher;
import pt.inevo.encontra.lucene.index.LuceneIndex;
import pt.inevo.encontra.query.QueryProcessorDefaultImpl;
import pt.inevo.encontra.rest.mapping.ImageDescriptorMap;
import pt.inevo.encontra.rest.utils.ImageModel;
import pt.inevo.encontra.rest.utils.ImageModelLoader;
import pt.inevo.encontra.storage.IEntity;
import pt.inevo.encontra.storage.IEntry;
import pt.inevo.encontra.storage.SimpleFSObjectStorage;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by jpvguerreiro on 10/30/2014.
 */
public class ClutchImageEngine<O extends IEntity, D extends DescriptorExtractor> extends ClutchAbstractEngine<O, D> {

    public ClutchImageEngine(String descString) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super();
        loader = new ImageModelLoader();
        type = "image";
        modelClass = ImageModel.class;

        D descriptor;
        if (descString == null){
            descString = "";
            descriptor = (D) new ColorLayoutDescriptor();
        }
        else {
            descString = descString.toUpperCase();

            ImageDescriptorMap descMap = ImageDescriptorMap.valueOf(descString);
            Class<?> descriptorClass = descMap.getFeatureClass();
            descriptor = (D) descriptorClass.getConstructor().newInstance();
        }

        String indexPath = "data/"+type+"/indexes/"+descString+"/";
        storage = new SimpleFSObjectStorage(modelClass, "data/"+type+"/objects/");
        SimpleSearcher<IndexedObject> searcher = new SimpleSearcher();
        lucIndex = new LuceneIndex(indexPath, descriptor.getClass());
        searcher.setDescriptorExtractor(descriptor);
        searcher.setIndex(lucIndex);
        setSearcher(type, searcher);
    }

    public ClutchImageEngine() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this(null);
    }

    @Override
    protected Result<O> getResultObject(Result<IEntry> entryResult) {
        return new Result<O>((O) storage.get( Long.parseLong(entryResult.getResultObject().getId().toString())));
    }

    public ImageModelLoader getLoader() {
        return (ImageModelLoader) this.loader;
    }

    public void setLoader(ImageModelLoader loader) {
        this.loader = loader;
    }

    public Class getModelClass() {
        return modelClass;
    }

    public void setModelClass(Class model) {
        this.modelClass = model;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void closeIndex() throws IOException {
        if(lucIndex != null) lucIndex.close();
    }

}