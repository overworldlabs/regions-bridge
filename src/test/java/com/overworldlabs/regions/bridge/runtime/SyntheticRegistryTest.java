package com.overworldlabs.regions.bridge.runtime;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SyntheticRegistryTest {
    
    @Test
    public void testSyntheticClassLoading() {
        // Using a shorter name to make bytecode management easier in this test
        String testClassName = "com.test.Dummy";
        String internalName = "com/test/Dummy";
        
        // Accurate Bytecode for a minimal class: com.test.Dummy extends Object
        byte[] dummyBytecode = new byte[] {
            (byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE, // Magic
            0, 0, 0, 61, // Version 17
            0, 5,       // Constant pool count (4 entries + 1 null)
            7, 0, 2,    // #1 Class com/test/Dummy
            1, 0, 14,   // #2 Utf8 com/test/Dummy (14 chars)
            'c','o','m','/','t','e','s','t','/','D','u', 'm', 'm', 'y',
            7, 0, 4,    // #3 Class java/lang/Object
            1, 0, 16,   // #4 Utf8 java/lang/Object
            'j','a','v','a','/','l','a','n','g','/','O', 'b', 'j', 'e', 'c', 't',
            0, 1,       // Access flags: public
            0, 1,       // This class: #1
            0, 3,       // Super class: #3
            0, 0,       // Interfaces count
            0, 0,       // Fields count
            0, 0,       // Methods count
            0, 0        // Attributes count
        };
        
        SyntheticClassRegistry.register(testClassName, dummyBytecode);
        
        try {
            LaunchEnvironment.create(
                this.getClass().getClassLoader(), 
                Thread.currentThread().getContextClassLoader()
            );
            LaunchEnvironment.get().captureRuntimeLoader(this.getClass().getClassLoader());

            MixinClassProvider provider = new MixinClassProvider();
            Class<?> clazz = provider.findClass(testClassName);
            
            assertNotNull(clazz, "Synthetic class should not be null");
            assertEquals(testClassName, clazz.getName(), "Class name should match");
        } catch (Throwable e) {
            e.printStackTrace();
            fail("Failed to load synthetic class: " + e.toString());
        }
    }
}
