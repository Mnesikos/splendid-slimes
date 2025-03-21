//package io.github.chakyl.splendidslimes.datagen;
//
//import io.github.chakyl.splendidslimes.SplendidSlimes;
//import io.github.chakyl.splendidslimes.registry.ModElements;
//import net.minecraft.data.PackOutput;
//import net.minecraftforge.common.data.ExistingFileHelper;
//import net.minecraftforge.common.data.LanguageProvider;
//
//
//public class ModLanguageProvider extends LanguageProvider {
//    public ModLanguageProvider(PackOutput output) {
//        super(output, SplendidSlimes.MODID, "en_us");
//    }
//
//    @Override
//    protected void addTranslations() {
//        ModElements.ITEMS.getEntries().forEach(entry -> {
//            addItem(entry, entry.getId().getPath());
//        });
//    }
//}