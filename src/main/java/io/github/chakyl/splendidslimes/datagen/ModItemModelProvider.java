//package io.github.chakyl.splendidslimes.datagen;
//
//import io.github.chakyl.splendidslimes.SplendidSlimes;
//import io.github.chakyl.splendidslimes.registry.ModElements;
//import net.minecraft.data.PackOutput;
//import net.minecraftforge.client.model.generators.ItemModelProvider;
//import net.minecraftforge.common.data.ExistingFileHelper;
//
//
//public class ModItemModelProvider extends ItemModelProvider {
//    public ModItemModelProvider(PackOutput output, ExistingFileHelper exFileHelper) {
//        super(output, SplendidSlimes.MODID, exFileHelper);
//    }
//
//    @Override
//    protected void registerModels() {
//        ModElements.ITEMS.getEntries().forEach(entry -> {
//
//            withExistingParent(entry.getId().getPath(), "item/generated").texture("layer0", "splendid_slimes:item/" + entry.getId().getPath());
//        });
//    }
//}