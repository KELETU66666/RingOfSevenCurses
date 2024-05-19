package keletu.cursedring.asm;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name(value = "CursedRingCore")
@IFMLLoadingPlugin.TransformerExclusions({"keletu.cursedring.asm"})
@IFMLLoadingPlugin.MCVersion("1.12.2")
public class CursedRingCore implements IFMLLoadingPlugin {

    public CursedRingCore() {

	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[]{"keletu.cursedring.asm.CRCoreTransformer"};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
	}

	@Override
	public String getAccessTransformerClass() {
		return "keletu.cursedring.asm.CRCoreTransformer";
	}

}