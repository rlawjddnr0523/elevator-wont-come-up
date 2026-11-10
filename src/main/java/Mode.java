public enum Mode {
    STANDARD(new StandardPolicy()),
    FAST(new FastPolicy()),
    ECO(new EcoPolicy());
    
    private final DispatchPolicy policy;
    
    Mode(DispatchPolicy policy) { this.policy = policy; }
    
    public DispatchPolicy policy() { return policy; }
}
