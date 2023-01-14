package mod.jesroads2.world;

import mod.jesroads2.tileentity.TileEntityRoadSign;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SignTemplateStorage {
    public static class SignDataTemplate {
        private TileEntityRoadSign.SignData[] templateData;

        public SignDataTemplate(){ }

        public SignDataTemplate(NBTTagCompound nbt){
            readFromNBT(nbt);
        }

        public void readFromNBT(NBTTagCompound nbt){
            int size = nbt.getInteger("size");
            templateData = new TileEntityRoadSign.SignData[size];
            for(int index = 0; index < size; index++){
                templateData[index] = new TileEntityRoadSign.SignData(nbt.getCompoundTag("item_" + index));
            }
        }

        public NBTTagCompound writeToNBT(){
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("size", templateData.length);
            int index = 0;
            for (TileEntityRoadSign.SignData data : templateData){
                nbt.setTag("item_"+(index++), data.getTag());
            }
            return nbt;
        }

        public TileEntityRoadSign.SignData[] getData(){
            TileEntityRoadSign.SignData[] data = new TileEntityRoadSign.SignData[templateData.length];
            for(int index = 0; index < data.length; index++) data[index] = templateData[index].getCopy();
            return data;
        }

        public void setData(TileEntityRoadSign.SignData[] data){
            templateData = data;
        }
    }

    private static final String fileName = "SignTemplates.dat";

    private static SignTemplateStorage instance;
    public static SignTemplateStorage getInstance(){
        if(instance == null) instance = new SignTemplateStorage();
        return instance;
    }

    private final File file;

    public Map<String, SignDataTemplate> templates;

    private SignTemplateStorage(){
        file = new File(fileName);
        templates = new HashMap<>();
        loadFromFile();
    }

    private void loadFromFile(){
        templates.clear();
        if(file.exists()){
            try {
                NBTTagCompound nbt = CompressedStreamTools.read(file);
                for (String key : nbt.getKeySet()) {
                    templates.put(key, new SignDataTemplate(nbt.getCompoundTag(key)));
                }
            }
            catch (Exception e){
                System.err.println("Failed to load template data");
                e.printStackTrace(System.err);
            }
        }
    }

    private void saveToFile(){
        NBTTagCompound nbt = new NBTTagCompound();
        for(Map.Entry<String, SignDataTemplate> item : templates.entrySet()){
            nbt.setTag(item.getKey(), item.getValue().writeToNBT());
        }

        try {
            CompressedStreamTools.write(nbt, file);
        }
        catch (Exception e){
            System.err.println("Failed to save template data");
            e.printStackTrace(System.err);
        }
    }

    public SignDataTemplate getTemplate(String name){
        return templates.get(name);
    }

    public String[] getTemplateNames(){
        return templates.keySet().toArray(new String[0]);
    }

    public void addTemplate(String name, SignDataTemplate template){
        templates.put(name, template);
        saveToFile();
    }

    public void addTemplate(String name, NBTTagCompound nbt){
        templates.put(name, new SignDataTemplate(nbt));
        saveToFile();
    }

    public void deleteTemplate(String name){
        templates.remove(name);
        saveToFile();
    }
}